# Running on OpenShift

This is a short documentation about how to spin up the Eclipse Kura™ simulator in OpenShift.

First of all you need some setup of [Eclipse Kapua™](https://eclipse.org/kapua "Eclipse Kapua™").
The easiest solution with OpenShift is to follow the readme in the Kapua GitHub repository: [dev-tools/src/main/openshift](https://github.com/eclipse/kapua/tree/develop/dev-tools/src/main/openshift "Setting up Kapua on OpenShift").

The rest of this documentation assumes that you did set up Kapua in OpenShift according to the above document.

    ./setup.sh

The script will create a new OpenShift application named `kura-simulator` which will connect to the broker in
the local project. By default every pod will spin up 10 gateway instances. You can start more pods which will then
multiply the instances:

    oc scale --replicas=10 dc kura-simulator

It is also possible to change the number of instances inside each pod by setting an environment variable:

    oc env dc/kura-simulator KSIM_NUM_GATEWAYS=100
