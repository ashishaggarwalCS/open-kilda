#!/usr/bin/env python
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

from clean_topology import cleanup
from create_topology import create_topo


#
# NB: This models the topology used in the flow acceptance tests. The standard flow topology is:
#       services/src/atdd/src/test/resources/topologies/nonrandom-topology.json
#   which is duplicated here:
#       services/topology-engine/queue-engine/tests/smoke-tests/flow-topology.json
#
# TODO: Create a single mechanism for deploying topologies .. whether python or jav
#
print "\n -- "
cleanup()
create_topo('flow-topology.json')
print "\n -- "