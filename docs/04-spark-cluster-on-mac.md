## Start a spark cluster

On Mac is pretty easy if you do the following:

- Enable SSH Remote Login (System Prefenceres -> Sharing -> Activate Remote Login)
- Optional: Copy you own public key to the authorized keys: ```cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys```
- Start Cluster with ./<SparkHome>/sbin/start-all.sh
	This start a cluster with a master and one worker.
	
	