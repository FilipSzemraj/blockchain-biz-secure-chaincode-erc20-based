# Docker & Docker Compose Command Reference

Quick reference for Docker commands useful in this Hyperledger Fabric project with ~24 containers across 3 organizations.

---

## Container Inspection & Access

### Enter a Running Container
```bash
# Interactive shell (bash)
docker exec -it <container_name> bash

# If bash not available, try sh
docker exec -it <container_name> sh

# Execute single command without entering
docker exec <container_name> <command>

# Examples for this project:
docker exec -it peer0.yachtsales.example.com bash
docker exec -it orderer.example.com bash
docker exec -it ca_orderer bash
```

### Detach and Reattach
```bash
# Detach from container without stopping it
# Press: Ctrl+P, then Ctrl+Q

# Reattach to a running container
docker attach <container_name>

# Note: Use 'exec' instead of 'attach' to avoid stopping container on exit
```

### Checking container env variables

```bash
docker exec peer0.furnituresmakers.com env 2>/dev/null | sort
```

or from images

```bash
docker run --rm hyperledger/fabric-javaenv:2.5.4 java -version 2>&1
```

---

## Container Logs

### Basic Log Commands
```bash
# View logs
docker logs <container_name>

# Follow logs in real-time
docker logs -f <container_name>

# Show last N lines
docker logs --tail 50 <container_name>

# Follow with last N lines
docker logs -f --tail 100 <container_name>

# Show timestamps
docker logs -t <container_name>

# Logs since specific time
docker logs --since 10m <container_name>    # Last 10 minutes
docker logs --since 2024-01-01 <container_name>

# Combine flags
docker logs -f --tail 100 -t <container_name>
```

### Project-Specific Examples
```bash
# Check peer chaincode logs
docker logs -f --tail 200 peer0.yachtsales.example.com

# Monitor orderer logs
docker logs -f --tail 100 orderer.example.com

# Check CA logs
docker logs -f ca_yachtsales

# Check configtxlator logs (channel creation)
docker logs configtxlator

# Backend API logs
docker logs -f client-api-container
docker logs -f bank-api-container
```

---

## Listing Containers

### Container Status
```bash
# List running containers
docker ps

# List all containers (including stopped)
docker ps -a

# Show only container IDs
docker ps -q
docker ps -aq    # All containers

# Filter by name
docker ps --filter "name=peer0"
docker ps -a --filter "name=yachtsales"

# Filter by status
docker ps --filter "status=exited"
docker ps --filter "status=running"

# Custom format
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# Show container sizes
docker ps -s
```

### Project-Specific Filters
```bash
# List all peer containers
docker ps --filter "name=peer"

# List all CA containers
docker ps --filter "name=ca_"

# List all orderer containers
docker ps --filter "name=orderer"

# List containers from specific org
docker ps --filter "name=yachtsales"
docker ps --filter "name=furnituresmakers"
docker ps --filter "name=woodsupply"
```

---

## Removing Containers

### Remove Single Container
```bash
# Remove stopped container
docker rm <container_name>

# Force remove running container
docker rm -f <container_name>

# Remove with volumes
docker rm -v <container_name>
```

### Bulk Removal
```bash
# Remove all stopped containers
docker container prune

# Remove all stopped containers without confirmation
docker container prune -f

# Remove specific stopped containers
docker rm $(docker ps -aq --filter "status=exited")

# Force remove all containers (DANGEROUS!)
docker rm -f $(docker ps -aq)

# Remove containers matching pattern
docker rm -f $(docker ps -aq --filter "name=peer0")
```

### Handle Name Conflicts
```bash
# Check if container with name exists
docker ps -a --filter "name=^/peer0.yachtsales.example.com$"

# Remove conflicting container before starting new one
docker rm -f peer0.yachtsales.example.com 2>/dev/null || true

# Script-friendly version
if docker ps -a --format '{{.Names}}' | grep -q "^peer0.yachtsales.example.com$"; then
    docker rm -f peer0.yachtsales.example.com
fi
```

---

## Images Management

### List Images
```bash
# List all images
docker images

# List all images (including intermediates)
docker images -a

# Show only image IDs
docker images -q

# Filter by repository
docker images hyperledger/fabric-peer
docker images hyperledger/fabric-orderer

# Show image sizes
docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}"

# Filter dangling images
docker images -f "dangling=true"
```

### Remove Images
```bash
# Remove specific image
docker rmi <image_name:tag>

# Force remove
docker rmi -f <image_name:tag>

# Remove dangling images
docker image prune

# Remove all unused images
docker image prune -a

# Remove images matching pattern
docker rmi $(docker images 'hyperledger/fabric-ca' -q)
```

### Project-Specific Images
```bash
# List Hyperledger Fabric images
docker images hyperledger/fabric-*

# List project chaincode images
docker images | grep erc20
docker images | grep dev-peer

# Check for stale chaincode images
docker images | grep "dev-peer.*erc20"
```

---

## Networks Management

### List Networks
```bash
# List all networks
docker network ls

# Inspect network details
docker network inspect <network_name>

# Show containers in network
docker network inspect <network_name> --format='{{range .Containers}}{{.Name}} {{end}}'
```

### Network Operations
```bash
# Create network
docker network create <network_name>

# Remove network
docker network rm <network_name>

# Remove unused networks
docker network prune

# Connect container to network
docker network connect <network_name> <container_name>

# Disconnect container from network
docker network disconnect <network_name> <container_name>
```

### Project-Specific Networks
```bash
# List Fabric networks
docker network ls | grep fabric

# Inspect main network (usually named after project directory)
docker network inspect blockchain-biz-secure-chaincode-erc20-based_default

# Check if network exists
docker network ls --filter "name=fabric_network" -q
```

---

## Docker Compose Commands

### Start/Stop Services
```bash
# Start services (from docker-compose.yml directory)
docker-compose up

# Start in background (detached)
docker-compose up -d

# Start specific services
docker-compose up -d peer0.yachtsales.example.com orderer.example.com

# Start with specific compose file
docker-compose -f docker-compose.yachtsales.yaml up -d

# Stop services
docker-compose stop

# Stop specific service
docker-compose stop peer0.yachtsales.example.com

# Stop and remove containers
docker-compose down

# Stop, remove containers and volumes
docker-compose down -v

# Stop, remove containers, volumes, and images
docker-compose down -v --rmi all
```

### Project-Specific Compose Operations
```bash
# Start Yacht Sales org
cd infrastructure/ca/
docker-compose -f docker-compose.yachtsales.yaml up -d

# Start all orgs
docker-compose -f docker-compose.yachtsales.yaml up -d
docker-compose -f docker-compose.furnituresmakers.yaml up -d
docker-compose -f docker-compose.woodsupply.yaml up -d

# Start orderers
docker-compose -f docker-compose.orderer.all.yaml up -d

# Start configtx service
docker-compose -f docker-compose.configtx.all.yaml up -d

# Start backend services
cd backend/
docker-compose up -d

# Restart specific org
docker-compose -f docker-compose.yachtsales.yaml restart

# View compose logs
docker-compose -f docker-compose.yachtsales.yaml logs -f --tail 100
```

### Rebuild and Update
```bash
# Rebuild images
docker-compose build

# Rebuild without cache
docker-compose build --no-cache

# Rebuild and start
docker-compose up -d --build

# Pull latest images
docker-compose pull

# Recreate containers
docker-compose up -d --force-recreate
```

### View Compose Status
```bash
# List services
docker-compose ps

# List all services (including stopped)
docker-compose ps -a

# Show service configuration
docker-compose config

# Validate compose file
docker-compose config -q
```

---

## Volume Management

### List and Inspect Volumes
```bash
# List volumes
docker volume ls

# Inspect volume
docker volume inspect <volume_name>

# Filter volumes
docker volume ls --filter "name=peer0"
```

### Remove Volumes
```bash
# Remove specific volume
docker volume rm <volume_name>

# Remove all unused volumes
docker volume prune

# Remove without confirmation
docker volume prune -f
```

---

## System Cleanup

### Clean Up Everything
```bash
# Remove stopped containers
docker container prune -f

# Remove dangling images
docker image prune -f

# Remove unused networks
docker network prune -f

# Remove unused volumes
docker volume prune -f

# Clean everything (containers, networks, images, volumes)
docker system prune -a --volumes -f
```

### Project-Specific Cleanup
```bash
# Stop all Fabric containers
docker stop $(docker ps -aq --filter "name=peer")
docker stop $(docker ps -aq --filter "name=orderer")
docker stop $(docker ps -aq --filter "name=ca_")

# Remove all Fabric containers
docker rm -f $(docker ps -aq --filter "name=peer")
docker rm -f $(docker ps -aq --filter "name=orderer")
docker rm -f $(docker ps -aq --filter "name=ca_")

# Remove chaincode containers
docker rm -f $(docker ps -aq --filter "name=dev-peer")

# Remove chaincode images
docker rmi -f $(docker images 'dev-peer*' -q)

# Full teardown (use with caution!)
cd infrastructure/ca/
./teardown-network.sh  # If script exists
# Or manually:
docker-compose -f docker-compose.yachtsales.yaml down -v
docker-compose -f docker-compose.furnituresmakers.yaml down -v
docker-compose -f docker-compose.woodsupply.yaml down -v
docker-compose -f docker-compose.orderer.all.yaml down -v
```

---

## Monitoring & Diagnostics

### Container Resource Usage
```bash
# Show container resource usage
docker stats

# Show specific containers
docker stats peer0.yachtsales.example.com orderer.example.com

# Single output (no streaming)
docker stats --no-stream

# Show all containers (including stopped)
docker stats -a
```

### Container Inspection
```bash
# Full container details
docker inspect <container_name>

# Get specific field
docker inspect --format='{{.State.Status}}' <container_name>
docker inspect --format='{{.NetworkSettings.IPAddress}}' <container_name>

# Check if container is running
docker inspect --format='{{.State.Running}}' <container_name>

# Get environment variables
docker inspect --format='{{range .Config.Env}}{{println .}}{{end}}' <container_name>

# Get mounted volumes
docker inspect --format='{{range .Mounts}}{{.Source}} -> {{.Destination}}{{println}}{{end}}' <container_name>
```

### Health Checks
```bash
# Check container health
docker inspect --format='{{.State.Health.Status}}' <container_name>

# View health check logs
docker inspect --format='{{range .State.Health.Log}}{{.Output}}{{end}}' <container_name>
```

---

## Debugging & Troubleshooting

### Copy Files To/From Container
```bash
# Copy from container to host
docker cp <container_name>:/path/in/container /path/on/host

# Copy from host to container
docker cp /path/on/host <container_name>:/path/in/container

# Example: Extract chaincode logs
docker cp peer0.yachtsales.example.com:/var/hyperledger/production/chaincodes ./chaincode-logs
```

### Check Container Processes
```bash
# Show running processes in container
docker top <container_name>

# Show processes with custom format
docker top <container_name> aux
```

### Network Connectivity
```bash
# Test network connectivity between containers
docker exec peer0.yachtsales.example.com ping -c 3 orderer.example.com

# Check DNS resolution
docker exec peer0.yachtsales.example.com nslookup orderer.example.com

# Check open ports
docker exec peer0.yachtsales.example.com netstat -tlnp
```

### DNS Resolution & Network Debugging

When containers can't connect, the first step is determining whether the issue is DNS resolution, network routing, or the target service itself.

```bash
# Check what IP a hostname resolves to from inside a container
# (nslookup may not be installed - getent is more reliable in minimal images)
docker exec <container_name> getent hosts <target_hostname>

# Example: Check if orderer resolves to Docker IP (172.x.x.x) or public IP
docker exec admin.woodsupply.com getent hosts orderer0.woodsupply.com
# Expected Docker IP: 172.18.0.x
# Public IP (e.g. 76.223.54.146) = DNS fallback to internet (see below)
```

**List all containers on a Docker network with their IPs:**
```bash
# Quick overview: container names and IPs
docker network inspect fabric-network \
  --format '{{range .Containers}}{{.Name}} -> {{.IPv4Address}}{{"\n"}}{{end}}'

# Full JSON details
docker network inspect fabric-network
```

**Check which networks a container is attached to:**
```bash
docker inspect <container_name> \
  --format '{{json .NetworkSettings.Networks}}' | python3 -m json.tool
```

**Docker events timeline — track container lifecycle during a time window:**
```bash
# See create/start/stop/die events for a specific container
docker events \
  --since="2026-02-14T13:10:00" \
  --until="2026-02-14T13:25:00" \
  --filter "container=orderer0.woodsupply.com" \
  --format "{{.Time}} {{.Action}} {{.Actor.Attributes.name}}"
```

**Compare container creation timestamps to understand startup ordering:**
```bash
docker ps -a \
  --filter "name=admin" \
  --filter "name=orderer" \
  --format "table {{.Names}}\t{{.Status}}\t{{.CreatedAt}}"
```

#### Known Issue: DNS Fallback to Public Internet

Docker's embedded DNS resolver will fall through to public DNS when a hostname is not found on any Docker network. If the domain happens to be a real registered domain (e.g. `woodsupply.com` is real, while `furnituresmakers.com` is not), the connection attempt goes to a public IP instead of failing fast with `no such host`.

**Symptoms:**
- Container hangs for ~5 minutes on a network call (TCP timeout to unreachable public IP)
- Other containers with non-existent domain names fail instantly with `no such host`
- After timeout, error shows a public IP: `dial tcp 76.223.54.146:9443: connect: connection refused`

**Diagnosis:**
```bash
# Check if a .com hostname resolves to Docker network or public internet
docker exec <container_name> getent hosts <hostname>

# Docker network IPs are typically 172.x.x.x or 10.x.x.x
# Any other IP = public DNS fallback
```

### Container Differences
```bash
# Show changes to container filesystem
docker diff <container_name>
```

---

## Quick Reference Scripts

### Check All Fabric Services Status
```bash
#!/bin/bash
echo "=== Orderers ==="
docker ps --filter "name=orderer" --format "table {{.Names}}\t{{.Status}}"

echo -e "\n=== CAs ==="
docker ps --filter "name=ca_" --format "table {{.Names}}\t{{.Status}}"

echo -e "\n=== Peers ==="
docker ps --filter "name=peer" --format "table {{.Names}}\t{{.Status}}"

echo -e "\n=== Chaincodes ==="
docker ps --filter "name=dev-peer" --format "table {{.Names}}\t{{.Status}}"
```

### Tail All Peer Logs
```bash
#!/bin/bash
# Tail logs from all peer containers
for peer in $(docker ps --filter "name=peer" --format "{{.Names}}"); do
    echo "=== Logs for $peer ==="
    docker logs --tail 20 "$peer"
    echo ""
done
```

### Network Health Check
```bash
#!/bin/bash
# Check if all expected containers are running
EXPECTED=(
    "orderer.example.com"
    "peer0.yachtsales.example.com"
    "peer0.furnituresmakers.example.com"
    "peer0.woodsupply.example.com"
    "ca_orderer"
    "ca_yachtsales"
    "ca_furnituresmakers"
    "ca_woodsupply"
)

for container in "${EXPECTED[@]}"; do
    if docker ps --format '{{.Names}}' | grep -q "^${container}$"; then
        echo "✓ $container is running"
    else
        echo "✗ $container is NOT running"
    fi
done
```

---

## Environment-Specific Commands

### Development
```bash
# Quick restart of changed service
docker-compose up -d --force-recreate <service_name>

# View real-time logs from multiple services
docker-compose logs -f peer0.yachtsales.example.com orderer.example.com

# Shell into backend API
docker exec -it client-api-container bash
```

### Testing
```bash
# Start with fresh state
docker-compose down -v
docker-compose up -d

# Isolate service for testing
docker-compose up -d <service_name>
docker-compose logs -f <service_name>
```

### Production Preparation
```bash
# Check resource consumption
docker stats --no-stream

# Export container logs
docker logs peer0.yachtsales.example.com > peer0-yachtsales.log 2>&1

# Backup volumes
docker run --rm -v peer0_data:/data -v $(pwd):/backup alpine tar czf /backup/peer0_data.tar.gz -C /data .
```

---

## Common Issues & Solutions

### Container Won't Start
```bash
# Check logs for errors
docker logs <container_name>

# Check if port is already in use
sudo netstat -tulpn | grep <port>

# Remove and recreate
docker rm -f <container_name>
docker-compose up -d <service_name>
```

### Container Name Conflict
```bash
# Remove existing container
docker rm -f <container_name>

# Or rename existing
docker rename <old_name> <new_name>
```

### Network Issues
```bash
# Recreate network
docker network rm <network_name>
docker network create <network_name>

# Reconnect container
docker network connect <network_name> <container_name>
```

### Out of Disk Space
```bash
# Check disk usage
docker system df

# Clean up
docker system prune -a --volumes -f

# Remove old chaincode containers and images
docker rm -f $(docker ps -aq --filter "name=dev-peer")
docker rmi -f $(docker images 'dev-peer*' -q)
```

---

## Best Practices

1. **Always use `-f` with compose files**: `docker-compose -f docker-compose.yaml`
2. **Check logs before removing**: Understand why a container failed
3. **Use `docker-compose down -v`**: Clean slate when troubleshooting
4. **Name your networks**: Avoid auto-generated names
5. **Tag your images**: Makes cleanup easier
6. **Use health checks**: Defined in docker-compose files
7. **Monitor resources**: Regular `docker stats` checks
8. **Backup volumes**: Before major changes
9. **Document compose file paths**: This project has multiple compose files
10. **Use scripts**: Automate common operations (start-network.sh, stop-network.sh)

---

## Project Structure Reminder

```
infrastructure/ca/
├── docker-compose.yachtsales.yaml
├── docker-compose.furnituresmakers.yaml
├── docker-compose.woodsupply.yaml
├── docker-compose.orderer.all.yaml
├── docker-compose.configtx.all.yaml
├── docker-compose.peer.all.yaml
└── docker-compose.api.yaml

backend/
├── client-api/
│   └── docker-compose.yaml (if exists)
└── bank-api/
    └── docker-compose.yaml (if exists)

frontend/
└── docker-compose.yaml (if exists - future)
```

Always execute docker-compose commands from the directory containing the compose file, or use `-f` flag with full path.
