# SOD ANALYSIS

Segregation of Duties (SOD) Risk Analysis

## I. Overview:
This project is a Java-based deterministic algorithm designed to analyze Segregation of Duties (SOD) risks in an organization's access control system. It processes user-role-privilege data from Excel reports to detect conflicts where users have access to conflicting entitlements, which could pose financial or security risks.

## II. Objective:
The primary goal is to identify and report SOD conflicts by analyzing the relationships between users, roles, privileges, and entitlements. This helps organizations mitigate the risk of fraud, unauthorized access, and policy violations in financial and operational systems.

## III. How It Works:
1.Data Ingestion
>Reads Excel files containing user-role mappings, role-privilege relationships, and role hierarchies.
>Loads predefined SOD conflict rules that specify conflicting privilege pairs.

2.Role & Privilege Processing
>Resolves role inheritance, ensuring all indirect role assignments are included.
>Maps roles to privileges and privileges to entitlements for access analysis.

3.Conflict Detection
>Compares each user's entitlements against the SOD ruleset.
>If a user has both entitlements in a conflict pair, a SOD violation is recorded.

4.Result Reporting
>Generates detailed reports on detected conflicts, listing users, roles, and conflicting access points.

## IV.Conclusion
This project provides an efficient, scalable, and deterministic method for SOD conflict detection, ensuring stronger internal controls and access governance in organizations. 

### HOW TO BUILD THIS PROJECT:

>javac -cp "libs/*;." SODAnalysis.java

### HOW TO RUN THIS PROJECT:

>java -cp "libs/*;." SODAnalysis <SOD ID>

#### DEPENDENCIES:

1. commons-collections4-4.4.jar – Provides additional collection utilities beyond Java’s standard collections framework.  
2. commons-compress-1.27.1.jar – Handles compression and archive formats like ZIP, TAR, and GZIP.  
3. commons-io-2.18.0.jar – Utilities for easier file and stream I/O operations.  
4. log4j-api-2.24.3.jar – Log4j API for logging in Java applications.  
5. log4j-core-2.24.3.jar – The core implementation of Log4j for actual logging functionality.  
6. ooxml-schemas-1.4.jar – Defines XML schemas for OpenXML formats used in Microsoft Office files.  
7. poi-5.3.0.jar – Apache POI library for reading and writing Microsoft Office documents.  
8. poi-ooxml-5.3.0.jar – POI extension for handling Office Open XML (OOXML) formats like .xlsx and .docx.  
9. poi-ooxml-full-5.3.0.jar – Full version of POI-OOXML including all dependencies.  
10. xmlbeans-5.3.0.jar – Apache XMLBeans for working with XML in Java using a Java object model.

## HOW TO BUILD DOCKER IMAGE:	
```
docker build -t my-java-app .
```
## HOW TO RUN DOCKER CONTAINER:
```
docker run --rm my-java-app  <SOD_ID> <userFile> <roleFile> <privsFile> <privToRoleFile> <roleToUserFile> <sodRulesFile> <roleHierarchyFile>
```






