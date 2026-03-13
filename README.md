# FastBoot-IDP

**FastBoot** is a lightweight **Internal Developer Platform (IDP)** built on Kubernetes that enables developers to **provision infrastructure resources declaratively using Custom Resources (CRDs)**. Instead of manually creating infrastructure components such as databases, developers can define them in Kubernetes manifests and let the **FastBoot Operator** automatically provision and manage them. The platform follows the **Kubernetes Operator pattern** and currently supports **database provisioning (PostgreSQL)** through a custom `Database` resource.

## Architecture Overview

FastBoot is structured as a modular platform:

~~~plaintext
fastboot-idp
 ├── fastboot-crds          # Kubernetes CRDs (Database, Platform)
 ├── fastboot-services      # Provisioning logic for resources
 ├── fastboot-controllers   # Reconciliation controllers
 ├── fastboot-operator      # Operator runtime
 ├── fastboot-core          # Shared platform abstractions
 └── fastboot-api           # Platform API contracts
~~~

## Key Concepts

**CRDs**

* `Database` – declarative database provisioning
* `Platform` – high-level platform resource grouping multiple services

**Provisioners**

* Resource provisioning is implemented via **SPI-style provisioners**
* Example: `PostgresProvisioner`

**Operator**

* Watches custom resources
* Reconciles desired state
* Creates Kubernetes resources such as:
  * StatefulSets
  * Services
  * Secrets

## Prerequisites

Before running FastBoot locally, ensure the following tools are installed:

* Kubernetes cluster via **Rancher Desktop**
* **Docker**
* **Helm**
* **Java 17+**
* **Maven**

After installing **Rancher Desktop**, make sure Kubernetes is running.

## Build the Project

Compile all modules and build the operator:

~~~bash
mvn clean install
~~~

This builds the FastBoot operator and supporting modules.

## Build Operator Docker Image

Build the Docker image used by the Kubernetes operator.

~~~bash
docker build -t fastboot-operator:1.0.0 .
~~~

****

This image will be deployed into the Kubernetes cluster.

## Deploy FastBoot Operator

Deploy the operator using Helm:

~~~bash
helm install fastboot . -n namespace
~~~

This command will:

* Install FastBoot CRDs
* Deploy the FastBoot Operator
* Start watching for platform resources

## Remove the Operator

To uninstall FastBoot from the cluster:

~~~bash
helm uninstall fastboot -n namespace
~~~

This removes the operator deployment from Kubernetes.

## Connecting to the Database

Once the database pod is running, you can connect using a database client like **DBeaver**.

Example using port-forward:

~~~bash
kubectl port-forward svc/service-name 5432:5432 -n namespace
~~~

Connection settings:

**Host: localhost<br>
Port: 5432<br>
Database: example<br>
User: from secret<br>
Password: from secret**<br>
