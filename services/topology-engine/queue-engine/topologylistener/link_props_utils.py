# Copyright 2018 Telstra Open Source
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

import logging
import textwrap

from topologylistener import db
from topologylistener import exc

logger = logging.getLogger(__name__)


def set_props(tx, subject, props):
    match = _make_match(subject)
    q = textwrap.dedent("""
        MATCH (target:link_props {
            src_switch: $src_switch,
            src_port: $src_port,
            dst_switch: $dst_switch,
            dst_port: $dst_port})
        RETURN target""")

    logger.debug('link_props lookup query:\n%s', q)
    cursor = tx.run(q, match)

    try:
        target = db.fetch_one(cursor)['target']
    except exc.DBEmptyResponse:
        raise exc.DBRecordNotFound(q, match)

    origin, update = db.locate_changes(target, props)
    if update:
        q = textwrap.dedent("""
        MATCH (target:link_props) 
        WHERE id(target)=$target_id
        """) + db.format_set_fields(
                db.escape_fields(update), field_prefix='target.')

        logger.debug('Push link_props properties: %r', update)
        tx.run(q, {'target_id': db.neo_id(target)})

        push_props_to_isl(tx, subject, *update.keys())

    return origin


def push_props_to_isl(tx, isl, *fields):
    copy_fields = {
        name: 'source.' + name for name in fields}
    q = textwrap.dedent("""
        MATCH (source:link_props) 
        WHERE source.src_switch = $src_switch
          AND source.src_port = $src_port
          AND source.dst_switch = $dst_switch
          AND source.dst_port = $dst_port
        MATCH
          (:switch {name: source.src_switch})
          -
          [target:isl {
            src_switch: source.src_switch,
            src_port: source.src_port,
            dst_switch: source.dst_switch,
            dst_port: source.dst_port
          }]
          ->
          (:switch {name: source.dst_switch})
        """) + db.format_set_fields(
            db.escape_fields(copy_fields, raw_values=True),
            field_prefix='target.')
    p = _make_match(isl)
    db.log_query()
    tx.run(q, p)


def _make_match(isl):
    return {
        'src_switch': isl.source.dpid,
        'src_port': isl.source.port,
        'dst_switch': isl.dest.dpid,
        'dst_port': isl.dest.port}
