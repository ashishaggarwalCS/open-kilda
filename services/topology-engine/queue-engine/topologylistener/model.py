# -*- coding:utf-8 -*-
# Copyright 2017 Telstra Open Source
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

import collections
import datetime
import json
import logging
import uuid
import weakref

import pytz

logger = logging.getLogger(__name__)


def convert_integer(raw):
    if isinstance(raw, (int, long)):
        return raw
    return int(raw, 0)


class Default(object):
    def __init__(self, value, produce=False, override_none=True):
        self._resolve_cache = weakref.WeakKeyDictionary()
        self.value = value
        self.produce = produce
        self.override_none = override_none

    def __get__(self, instance, owner):
        if instance is None:
            return self

        value = self.value
        if self.produce:
            value = value()

        setattr(instance, self._resolve_name(owner), value)
        return value

    def is_filled(self, instance):
        name = self._resolve_name(type(instance))
        data = vars(instance)
        return name in data

    def _resolve_name(self, owner):
        try:
            return self._resolve_cache[owner]
        except KeyError:
            pass

        for name in dir(owner):
            try:
                attr = getattr(owner, name)
            except AttributeError:
                continue
            if attr is not self:
                continue
            break
        else:
            raise RuntimeError(
                '{!r} Unable to resolve bounded name (UNREACHABLE)'.format(
                    self))

        self._resolve_cache[owner] = name
        return name


class Abstract(object):
    pack_exclude = frozenset()

    def __init__(self, **fields):
        cls = type(self)
        extra = set()
        for name in fields:
            extra.add(name)
            try:
                attr = getattr(cls, name)
            except AttributeError:
                continue
            if not isinstance(attr, Default):
                continue

            extra.remove(name)

            if attr.override_none and fields[name] is None:
                continue
            setattr(self, name, fields[name])

        if extra:
            raise TypeError('{!r} got unknown arguments: "{}"'.format(
                self, '", "'.join(sorted(extra))))

    def __str__(self):
        return '<{}:{}>'.format(
            type(self).__name__,
            json.dumps(self.pack(), sort_keys=True, cls=JSONEncoder))

    def pack(self):
        payload = vars(self).copy()

        cls = type(self)
        for name in dir(cls):
            if name.startswith('_'):
                continue
            if name in payload:
                continue

            attr = getattr(cls, name)
            if not isinstance(attr, Default):
                continue
            payload[name] = getattr(self, name)

        for name in tuple(payload):
            if not name.startswith('_') and name not in self.pack_exclude:
                continue
            del payload[name]

        return payload

    def _sort_key(self):
        raise NotImplementedError


class NetworkEndpoint(Abstract):
    @classmethod
    def new_from_isl_data_path(cls, path_node):
        return cls(path_node['switch_id'], path_node['port_no'])

    @classmethod
    def new_from_java(cls, data):
        return cls(data['switch-id']. data['port-id'])

    def __init__(self, dpid, port, **fields):
        super(NetworkEndpoint, self).__init__(**fields)
        self.dpid = dpid.lower()
        self.port = int(port, 0)

    def __str__(self):
        return '{}-{}'.format(self.dpid, self.port)

    def _sort_key(self):
        return self.dpid, self.port


class AbstractLink(Abstract):
    def __init__(self, source, dest, **fields):
        super(AbstractLink, self).__init__(**fields)
        self.source = source
        self.dest = dest

    def _sort_key(self):
        return self.source, self.dest


class LinkProps(AbstractLink):
    isl_protected_fields = frozenset((
        'latency', 'speed', 'available_bandwidth', 'status'))
    props_converters = {
        'cost': convert_integer}

    props = Default(dict, produce=True)

    @classmethod
    def new_from_java(cls, data):
        data = data.copy()
        source = NetworkEndpoint.new_from_java(data.pop('source'))
        dest = NetworkEndpoint.new_from_java(data.pop('dest'))
        props = data.pop('props', dict()).copy()
        return cls(source, dest, props=props, **data)

    @classmethod
    def new_from_db(cls, data):
        data = data.copy()
        endpoints = []
        for prefix in ('src_{}', 'dst_'):
            dpid = data[prefix + 'switch']
            port = data[prefix + 'port']
            endpoints.append(NetworkEndpoint(dpid, port))
        source, dest = endpoints
        props = data.get('props', dict()).copy()
        return cls(source, dest, props=props)

    def __init__(self, source, dest, **fields):
        super(LinkProps, self).__init__(**fields)
        self.source = source
        self.dest = dest

        self._filter_props()
        self._decode_props()

    def _filter_props(self):
        filtered = set()
        for field in self.isl_protected_fields:
            try:
                del self.props[field]
                filtered.add(field)
            except KeyError:
                pass
        if filtered:
            logger.warning(
                'Filter out %s fields from object %s',
                ', '.join(repr(x) for x in sorted(filtered)),
                self)

    def _decode_props(self):
        for field, converter in self.props_converters.items():
            try:
                value = self.props[field]
            except KeyError:
                continue
            self.props[field] = converter(value)


class InterSwitchLink(
        collections.namedtuple(
            'InterSwitchLink', ('source', 'dest', 'state'))):

    @classmethod
    def new_from_isl_data(cls, isl_data):
        try:
            path = isl_data['path']
            endpoints = [
                NetworkEndpoint.new_from_isl_data_path(x)
                for x in path]
        except KeyError as e:
            raise ValueError((
                 'Invalid record format "path": is not contain key '
                 '{}').format(e))

        if 2 == len(endpoints):
            pass
        elif 1 == len(endpoints):
            endpoints.append(None)
        else:
            raise ValueError(
                'Invalid record format "path": expect list with 1 or 2 nodes')

        source, dest = endpoints
        return cls(source, dest, isl_data['state'])

    @classmethod
    def new_from_db(cls, link):
        source = NetworkEndpoint(link['src_switch'], link['src_port'])
        dest = NetworkEndpoint(link['dst_switch'], link['dst_port'])
        return cls(source, dest, link['status'])

    def ensure_path_complete(self):
        ends_count = len(filter(None, (self.source, self.dest)))
        if ends_count != 2:
            raise ValueError(
                'ISL path not define %s/2 ends'.format(ends_count))

    def reversed(self):
        cls = type(self)
        return cls(self.dest, self.source, self.state)

    def __str__(self):
        return '{} <===> {}'.format(self.source, self.dest)


LifeCycleFields = collections.namedtuple('LifeCycleFields', ('ctime', 'mtime'))


class TimeProperty(object):
    FORMAT = '%Y-%m-%dT%H:%M:%S.%fZ'
    UNIX_EPOCH = datetime.datetime(1970, 1, 1, 0, 0, 0, 0, pytz.utc)

    @classmethod
    def new_from_java_timestamp(cls, value):
        value = int(value)
        value /= 1000.0
        return cls(datetime.datetime.utcfromtimestamp(value))

    @classmethod
    def new_from_db(cls, value):
        value = datetime.datetime.strptime(value, cls.FORMAT)
        return cls(value)

    @classmethod
    def now(cls, milliseconds_precission=False):
        value = datetime.datetime.utcnow()
        if milliseconds_precission:
            microseconds = value.microsecond
            microseconds -= microseconds % 1000
            value = value.replace(microsecond=microseconds)
        return cls(value)

    def __init__(self, value):
        if value.tzinfo is None:
            value = value.replace(tzinfo=pytz.utc)
        self.value = value

    def __str__(self):
        return self.value.strftime(self.FORMAT)

    def as_java_timestamp(self):
        from_epoch = self.value - self.UNIX_EPOCH
        seconds = int(from_epoch.total_seconds())
        return seconds * 1000 + from_epoch.microseconds // 1000


class JsonSerializable(object):
    pass


class JSONEncoder(json.JSONEncoder):
    def default(self, o):
        if isinstance(o, TimeProperty):
            value = str(o)
        elif isinstance(o, uuid.UUID):
            value = str(o)
        elif isinstance(o, JsonSerializable):
            value = vars(o)
        elif isinstance(o, Abstract):
            value = o.pack()
        else:
            value = super(JSONEncoder, self).default(o)
        return value
