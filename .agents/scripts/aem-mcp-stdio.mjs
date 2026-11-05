#!/usr/bin/env node
/*******************************************************************************
 *
 *    Copyright 2026 Adobe. All rights reserved.
 *    This file is licensed to you under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License. You may obtain a copy
 *    of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software distributed under
 *    the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 *    OF ANY KIND, either express or implied. See the License for the specific language
 *    governing permissions and limitations under the License.
 *
 ******************************************************************************/
import { StdioServerTransport } from "/home/anand.parmar@brainvire.com/.nvm/versions/node/v20.19.0/lib/node_modules/@netcentric/aem-mcp-server/node_modules/@modelcontextprotocol/sdk/dist/esm/server/stdio.js";
import { createMCPServer } from "/home/anand.parmar@brainvire.com/.nvm/versions/node/v20.19.0/lib/node_modules/@netcentric/aem-mcp-server/dist/mcp/mcp.server.js";

const host = process.env.AEM_HOST || "http://localhost:4502";
const user = process.env.AEM_USER || "admin";
const pass = process.env.AEM_PASSWORD || "admin";
const id = process.env.AEM_CLIENT_ID || "";
const secret = process.env.AEM_CLIENT_SECRET || "";

const server = createMCPServer({ host, user, pass, id, secret });
const transport = new StdioServerTransport();
await server.connect(transport);

