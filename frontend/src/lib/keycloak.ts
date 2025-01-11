import Keycloak, {KeycloakConfig, KeycloakInstance} from "keycloak-js";

const keycloakConfig: KeycloakConfig = {
    url: 'http://localhost:8080', // URL of the Keycloak server
    realm: 'just-donate',  // Realm
    clientId: 'frontend-client' // Client ID
}

const keycloak: Keycloak = new Keycloak(keycloakConfig);

export default keycloak;