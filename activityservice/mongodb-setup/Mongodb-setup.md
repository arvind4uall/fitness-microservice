# MongoDB on Kubernetes (Colima)

My setup for running MongoDB on a local Kubernetes cluster using Colima, with Compass for the GUI.

## Prerequisites

- Colima running with Kubernetes enabled (`colima start --kubernetes`)
- `kubectl` configured
- MongoDB Compass installed — grab it from https://www.mongodb.com/products/compass

---

## Cluster Setup

### Create a namespace

```bash
kubectl create namespace mongodb
```

### Create credentials as a secret

```bash
kubectl create secret generic mongo-secret \
  --namespace=mongodb \
  --from-literal=MONGO_INITDB_ROOT_USERNAME=root \
  --from-literal=MONGO_INITDB_ROOT_PASSWORD=rootpass
```

### Apply all the YAML files

Make sure you're in the directory where the YAML files are, then:

```bash
kubectl apply -f . -n mongodb
```

This picks up the PVC, Deployment, and Service definitions in one shot.

### Verify

```bash
kubectl get all -n mongodb
```

Everything should show `Running` for the pod and the service should be listed.

---

## YAML Files Breakdown

### `mongo-pvc.yaml` — persistent storage

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mongo-pvc
  namespace: mongodb
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 5Gi
```

Without this, data dies with the pod.

### `mongo-deployment.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mongodb
  namespace: mongodb
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mongodb
  template:
    metadata:
      labels:
        app: mongodb
    spec:
      containers:
        - name: mongodb
          image: mongo:latest
          ports:
            - containerPort: 27017
          envFrom:
            - secretRef:
                name: mongo-secret
          volumeMounts:
            - name: mongo-storage
              mountPath: /data/db
      volumes:
        - name: mongo-storage
          persistentVolumeClaim:
            claimName: mongo-pvc
```

### `mongo-service.yaml` — internal access

```yaml
apiVersion: v1
kind: Service
metadata:
  name: mongodb-service
  namespace: mongodb
spec:
  selector:
    app: mongodb
  ports:
    - port: 27017
      targetPort: 27017
  type: ClusterIP
```

ClusterIP is fine here — we don't need to expose Mongo outside the cluster. We'll use port-forwarding for local access.

---

## Connecting with Compass

MongoDB is inside the cluster, and Compass is on your machine. They can't see each other directly. Port-forward bridges that gap:

```bash
kubectl port-forward svc/mongodb-service 27017:27017 -n mongodb
```

This opens a tunnel — your `localhost:27017` now routes straight into the pod. Keep this terminal open.

Connection string for Compass:

```
mongodb://root:rootpass@localhost:27017/?authSource=admin
```

> **Note:** The moment you close the port-forward terminal, Compass loses connection. Mongo is still running, you just can't reach it from outside the cluster.

---

## Mongo Express (browser-based GUI, optional)

If you don't want to install Compass, drop this in as `mongo-express.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mongo-express
  namespace: mongodb
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mongo-express
  template:
    metadata:
      labels:
        app: mongo-express
    spec:
      containers:
        - name: mongo-express
          image: mongo-express:latest
          ports:
            - containerPort: 8081
          env:
            - name: ME_CONFIG_MONGODB_ADMINUSERNAME
              value: root
            - name: ME_CONFIG_MONGODB_ADMINPASSWORD
              value: rootpass
            - name: ME_CONFIG_MONGODB_URL
              value: "mongodb://root:rootpass@mongodb-service:27017/"
---
apiVersion: v1
kind: Service
metadata:
  name: mongo-express-service
  namespace: mongodb
spec:
  selector:
    app: mongo-express
  ports:
    - port: 8081
      targetPort: 8081
  type: NodePort
```

Then port-forward in a separate terminal:

```bash
kubectl port-forward svc/mongo-express-service 8081:8081 -n mongodb
```

Open http://localhost:8081 in the browser.

---

## Spring Boot Connection

Add the dependency:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

In `application.yml`:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://root:rootpass@localhost:27017/your_db_name?authSource=admin
```

If the Spring Boot app is running **inside the same K8s cluster**, use the internal DNS instead of localhost:

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://root:rootpass@mongodb-service.mongodb.svc.cluster.local:27017/your_db_name?authSource=admin
```

---

## Quick Reference

| Action | Command |
|---|---|
| Start everything | `kubectl apply -f . -n mongodb` |
| Check status | `kubectl get all -n mongodb` |
| Port-forward Mongo | `kubectl port-forward svc/mongodb-service 27017:27017 -n mongodb` |
| Port-forward Express | `kubectl port-forward svc/mongo-express-service 8081:8081 -n mongodb` |
| View logs | `kubectl logs -f deployment/mongodb -n mongodb` |
| Shell into Mongo pod | `kubectl exec -it deployment/mongodb -n mongodb -- mongosh -u root -p rootpass` |
| Tear it all down | `kubectl delete namespace mongodb` |

---

## Cleanup

To remove everything:

```bash
kubectl delete namespace mongodb
```

This wipes the namespace and everything inside it — pods, services, secrets, PVC, all of it.