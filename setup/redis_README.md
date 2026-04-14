# Not longer in use due to already dockerized

# Redis
The introduciton of Redis to this project is to implement locking with key. This prevents "Race Conditions" and stops your database from being overwhelmed by duplicate requests.

## Install Redis in Window with Docker
**1. Open Command Prompt**
```sql
docker run -d -p 6379:6379 --name redis redis
```
    
**Note: If you already have "redis" container running, Docker will give you an error saying:**

**"The container name "/redis" is already in use by container XXX."**


**2. Enter Redis CLI**
```sql
docker exec -it redis redis-cli
```

**3. After enter CLI, test ping**
```sql
ping
```
Expected result: 
```sql
pong
```

## Redis Replica

### To implement redis replica, they must in the same network.

**1. Create docker network**
```sql
docker network create redis-net
```

**2. Create the master docker instance that within the network**
```sql
docker run -d --name redis --network redis-net redis
```

**3. Create the replica docker instance within the same network**
```sql
docker run -d --name my-replica --network redis-net redis redis-server --replicaof redis 6379
```

> Note: If you run use docker to create instance without `-p`, the application outside of Docker will not be able to see the docker instance.

You may run with `-p` flag if your app not dockerize yet.
```sql
docker run -d -p 6379:6379 --name redis --network redis-net redis
```
Replica 1
```sql
docker run -d --name my-replica -p 6380:6379 --network redis-net redis redis-server --replicaof redis 6379
```
>If you have more replica, just make sure they are in different port and point to 6379 master node

### Port Mapping Logic
- **Host Port (Left side):** Must be unique (6379, 6380, 6381) so Windows can distinguish between containers.
- **Container Port (Right side):** Almost always 6379, as that is the default Redis port.
- **Replication Target:** Always points to the Master's internal name (`redis-master`) and internal port (`6379`).

### To verify if you setup correctly

    1. docker exec -it redis redis-cli info replication

    2. docker exec -it my-replica redis-cli info replication

### Verify your master node and replica
**1. Check your master node**
```sql
docker exec -it redis redis-cli info replication
```
Example result: 
> You should see this instance role: master
```
# Replication
role:master
connected_slaves:1
slave0:ip=172.19.0.3,port=6379,state=online,offset=210,lag=1,io-thread=0
master_failover_state:no-failover
master_replid:c7780e3f971cc21346b1e55d096f9e6960a36230
master_replid2:0000000000000000000000000000000000000000
master_repl_offset:210
second_repl_offset:-1
repl_backlog_active:1
repl_backlog_size:1048576
repl_backlog_first_byte_offset:1
repl_backlog_histlen:210
```

**2. Check your replica node**
```sql
docker exec -it my-replica redis-cli info replication
```
Example result:
> You should see this instance role: slave
```
# Replication
role:slave
master_host:redis
master_port:6379
master_link_status:up
master_last_io_seconds_ago:9
master_sync_in_progress:0
slave_read_repl_offset:210
slave_repl_offset:210
replica_full_sync_buffer_size:0
replica_full_sync_buffer_peak:0
master_current_sync_attempts:1
master_total_sync_attempts:1
master_link_up_since_seconds:149
master_client_io_thread:0
total_disconnect_time_sec:0
slave_priority:100
slave_read_only:1
replica_announced:1
connected_slaves:0
master_failover_state:no-failover
master_replid:c7780e3f971cc21346b1e55d096f9e6960a36230
master_replid2:0000000000000000000000000000000000000000
master_repl_offset:210
second_repl_offset:-1
repl_backlog_active:1
repl_backlog_size:1048576
repl_backlog_first_byte_offset:15
repl_backlog_histlen:196
```

## Redis Sentinel

Known issue:
> Stale data in sentinel causing Java program listening to wrong IP
> Temp Solution: docker-compose down -v  AND docker-compose up -d, this will delete all cache data 

## Redis Command

### Set if not existed, and expiration in 10 sec
```sql
SET USER:1:ORD_1234 1 NX EX 10
```
Result:
```
Set successfully - Return 'OK'
Set failed - Return '(nil)'
```

### Get the value of the key
```sql
GET USER:1:ORD_1234
```
Result:
```
Get success - Return the value of the key
Get failed - Return '(nil)'
```

### Check Time-To-Live (TTL) of key
```sql
TTL USER:1:ORD_1234
```
Result:
```
If key not existed - Return "(integer) -2"
If key existed but no expiration time - Return "(integer) -1"
If key exsited and has expiration time - Return the remaining second
```

### Check all key that redis holding
All Key
```sql
KEYS *
```
All key that startwith
```sql
KEYS USER:*
```

#### Warning:
Never use KEYS * in a production environment with millions of keys. It is a "blocking" command and can freeze your Redis server. Always use SCAN instead.


#### KEYS vs SCAN
> **Problem:** Redis is single-threaded. `KEYS *` blocks the entire server until it finishes scanning every key ($O(N)$). If you have millions of keys, your app will hang.

> **Solution:** Use `SCAN 0 MATCH pattern* COUNT 100`. It iterates in small batches, allowing other operations to process in between.
The complexity of KEYS * is $O(N)$, where $N$ is the total number of keys in your database.
    
### Scan key
```sql
SCAN 0 MATCH USER:* COUNT 100
```

### Check how many key inside Redis
```sql
DBSIZE
```

### Check docker existing IP
>docker inspect -f '{{.Name}} - {{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $(docker ps -aq)

### Check sentinel on who is master now
>docker exec -it sentinel-1 redis-cli -p 26379 sentinel get-master-addr-by-name mymaster

## Common issue

1. Java program failed to connect redis
```
Request processing failed: org.springframework.data.redis.RedisConnectionFailureException: Unable to connect to Redis] with root cause
```

> Solution: Probably your java program is not being dockerized yet, and your redis instance didn't setup port, so those program outside docker cannot see the instance.