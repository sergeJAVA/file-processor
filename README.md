# File-processor
## Description:

File-processor-service provides a second validation step,
during which the contents of the file are validated. After validation is completed,
the service notifies another file-status-processor service that the second validation step has been successful and the file can be uploaded.

---

**Before you start the service you need to make sure that all data servers are up and running.**

---

**The docker-compose.yml file for running data servers is located in the `file-uploader` service. It must be launched before all services are started.**

---


**The application port is 8082**

---
