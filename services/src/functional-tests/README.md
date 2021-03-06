# Functional tests
This module holds Functional tests designed to be run against staging OR virtual environment.

#Word about testing approach
### Single topology for the whole test suite
Since this test suite should have ability to be run both on hardware and virtual topology,
we consider that we have the same amount of switches/same topology throughout the run even 
for virtual runs (obviously we cannot change the topology during a hardware run).  
Topology scheme is defined via special config file (topology.yaml) and remains the same throughout 
the test run.  
For this reason we cannot allow tests to assume they will have a 'needed' topology, so each
test should be designed to work on ANY topology (or skip itself if unable to run on given topology).
Some tests require a 'special' topology state (no alternative paths, isolated switches etc.). 
This can be achieved by manipulating existing topology via so-called A-Switch(transit switch not
 connected to controller, allows to change 
ISLs between switches) or controlling ports on switches (bring ports down to fail certain ISLs). 
It is required to bring the topology to the original state afterwards.

### Failfast with no cleanup
We do not do a 'finally' cleanup. Any cleanup steps are usually part of the test itself and they 
are **not** run if the test fails somewhere in the middle.  
In case of failure, any subsequent tests are skipped. This allows to diagnose the 'broken' system state when the test failed.  
The drawback is that the engineer will have to manually bring the system/topology back to its original
state after analysing the test failure (usually not an issue for virtual topology since it is 
recreated at the start of the test run).

# How to run 
Pre-setup: Mark `groovy` subdirectory in `functional-tests` module as a test sources root and ensure that `topology.yaml` and 
`kilda.properties` are present in the root of functional-tests
### Virtual (local Kilda)
- Spawn your Kilda env locally by running
```
make build-latest 
make up-test-mode
```
- Check your `kilda.properties`. It should point to your localhost environments.  
`spring.profiles.active` should be set to `virtual`.
- Check your `topology.yaml`. This is a file which will be used to spawn a virtual
topology used by all the tests.
- You can now run tests from IDE or run  
`mvn clean test -Pfunctional`

### Hardware (Staging)
- Check your `kilda.properties`. It should point to your staging environments.  
`spring.profiles.active` should be set to `hardware`.
- Check your `topology.yaml`. It should represent your actual hardware topology.
- You can now run tests from IDE or run  
`mvn clean test -Pfunctional`

## Artifacts
* Logs - ```target/logs```
* Reports - ```target/spock-reports```

# Deployment
## Confirguration
### Topology
The tests require a network topology definition provided.
For hardware(staging) topology this definition should represent the actual state of the hardware topology
For virtual(mininet) topology this definition will serve as a guide for creating a virtual topology.

The topology definition format:
```
switches:
    - name: ofsw1
      dp_id: 00:00:00:00:00:00:00:01
      of_version: OF_13
      status: active
      out_ports:
        - port: 10
          vlan_range: 101..150

    - name: ofsw2
      dp_id: 00:00:00:00:00:00:00:02
      of_version: OF_13
      status: active
      out_ports:
        - port: 10
          vlan_range: 101..150
isls:
    - src_switch: ofsw1
      src_port: 2
      dst_switch: ofsw2
      dst_port: 4
      max_bandwidth: 36000000
    - src_switch: ofsw1
      src_port: 1
      dst_switch: ofsw2
      dst_port: 1
      a_switch:
        in_port: 49
        out_port: 50
      max_bandwidth: 36000000
    - src_switch: ofsw1
      src_port: 5
      max_bandwidth: 36000000
      a_switch:
        in_port: 20
traffgens:
    - name: tg1
      iface: eth0
      control_endpoint: http://localhost:4041
      switch: ofsw1
      switch_port: 10
      status: active
    - name: tg2
      iface: eth0
      control_endpoint: http://localhost:4042
      switch: ofsw2
      switch_port: 10
      status: active

traffgen_config:
    address_pool_base: 0.0.0.0
    address_pool_prefix_len: 20
```

### Kilda configuration
The tests require Kilda configuration provided. See `kilda.properties.example`

