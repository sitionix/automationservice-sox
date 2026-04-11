# automationservice-sox

Minimal Automation bounded-context foundation for Sitionix.

Current scope:
- `Agent` persistence in Postgres
- `POST /api/v1/agents`
- `GET /api/v1/agents`
- `GET /api/v1/agents/{agentId}`

Explicit non-goals:
- playbooks
- memory
- orchestration
- execution engine
- runs and workflows
- scheduling
- prompts and external integrations

Local runtime touchpoints:
- service context path: `/atmssox`
- service port: `9083`
- local database: `AUTOMATION_SOX` on `5436`
- Flyway migrations: [`db-migration`](/Users/vladvinskevitch/Documents/Java/sitionix/automationservice-sox/db-migration)

Workspace route:
- `/workspace/automation`
# automationservice-sox
