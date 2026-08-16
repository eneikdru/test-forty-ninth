<!-- markdownlint-disable MD013 -->
# ADR: Document Storage Architecture for Epidemiological Knowledge Base

## Context

The "Epidemiological Knowledge Base" needs to store large PDF protocols, epidemiological materials, and data. We are evaluating object storage versus database BLOBs for this document storage to ensure the cataloging system is scalable and performant.

## Decision

We will use **object storage** (e.g., S3-compatible storage) instead of database BLOBs for storing large PDF protocols and other documents. The database will only store metadata and references (URIs) to the objects in the object storage.

## Rationale

* **TOC (Theory of Constraints):** The database is typically the primary bottleneck in scaling web applications. Storing large binary files in the database increases I/O load, memory pressure, and backup sizes, exacerbating this constraint. Offloading file storage to a dedicated object storage service protects the database's transaction throughput.
* **Lean Value:** Object storage is significantly cheaper per GB than database storage. Storing BLOBs in the database would be classified as `waste` (overprocessing and unnecessary resource utilization). Object storage also scales horizontally without affecting the database.
* **Performance:** Object storage provides predictable latency for large file retrieval and allows for straightforward CDN integration, which will improve download times for large PDFs.

## Handoff

**Next Owner Role:** BARCAN-TAG-01
