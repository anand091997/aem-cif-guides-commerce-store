# AEM Model Context Protocol (MCP) Integration

This folder contains workspace configurations for integrating Adobe Experience Manager (AEM) with Antigravity and other AI IDEs/assistants using the Model Context Protocol (MCP).

## Server Details
- **Provider:** `@netcentric/aem-mcp-server`
- **Transport:** Stdio (Local agent process) & Streamable HTTP (Port 8502)
- **Target Instance:** `http://localhost:4502`
- **Credentials:** `admin:admin` (Configurable via `AEM_USER` / `AEM_PASSWORD`)

## Script
- `scripts/aem-mcp-stdio.mjs`: Node.js Stdio MCP server runner that connects Antigravity directly to local AEM.

## Available Tools
- `listPages`, `getPageContent`, `getPageProperties`, `getPageTextContent`, `getAllTextContent`, `getPageImages`
- `searchContent` (QueryBuilder), `executeJCRQuery`, `enhancedPageSearch`
- `createPage`, `deletePage`
- `createComponent`, `deleteComponent`, `updateComponent`, `updateImagePath`, `scanPageComponents`
- `convertComponent`, `bulkConvertComponents`
- `listWorkflowModels`, `startWorkflow`, `listWorkflowInstances`, `getWorkflowInstance`, `updateWorkflowInstanceState`
- `getInboxItems`, `completeWorkItem`, `delegateWorkItem`, `getWorkItemRoutes`
- `fetchSites`, `fetchLanguageMasters`, `fetchAvailableLocales`

