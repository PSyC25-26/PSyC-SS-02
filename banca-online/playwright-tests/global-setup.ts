import { assertBackendReachable } from './support/healthcheck';

export default async function globalSetup() {
  await assertBackendReachable();
}
