### Prerequisites

- Node >= 12
- [Tiingo](https://api.tiingo.com/) API Token
- [NewsAPI](https://newsapi.org/) API Key

### Development

- Update Tiingo API Token and NewsAPI API Key in `server/src/secrets/index.ts`.
- Install dependencies using `npm install`.
- Build application using `npm run build`.
- Start application using `npm run start`.
- Clean application using `npm run clean`.

### Deployment

#### AWS

- Update Tiingo API Token and NewsAPI API Key in `server/src/secrets/index.ts`.
- Install dependencies using `npm install`.
- Build application using `npm run build`.
- Build `eb-deploy.zip` using `npm run build-zip:aws`. Deploy it by uploading manually. Remove it using `npm run clean-zip:aws`.
- Clean application using `npm run clean`.

#### Azure

- Update Tiingo API Token and NewsAPI API Key in `server/src/secrets/index.ts`.
- Install dependencies using `npm install`.
- Build application using `npm run build`.
- Delete `node_modules` using `npx rimraf node_modules` because the next command uploads the entire project directory.
- Create and deploy application using `az webapp up --sku F1 --name usc-csci-571-hw8-<unique> --location westus`. It saves information in `.azure/config`.
- Use `az webapp up` when you just want to update application.
- Clean application using `npm run clean`.

#### Google Cloud

- Update Tiingo API Token and NewsAPI API Key in `server/src/secrets/index.ts`.
- Install dependencies using `npm install`.
- Build application using `npm run build`.
- Create project using `gcloud projects create usc-csci-571-hw8-<unique> --set-as-default` in `us-west-2` region.
- Create application in above project using `gcloud app create --project=usc-csci-571-hw8-<unique>`.
- Deploy application using `gcloud app deploy`.
- Clean application using `npm run clean`.
