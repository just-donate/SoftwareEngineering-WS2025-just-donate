import keycloak from '@/lib/keycloak';
import React, { createContext, ReactNode } from 'react';

interface KeycloakContextProps {
  keycloak: typeof keycloak;
  initialized: boolean;
  authenticated: boolean;
}

const KeycloakContext = createContext<KeycloakContextProps | undefined>(
  undefined,
);

interface KeycloakProviderProps {
  children: ReactNode;
}

export const KeycloakProvider: React.FC<KeycloakProviderProps> = ({
  children,
}) => {
  const [initialized, setInitialized] = React.useState(false);
  const [authenticated, setAuthenticated] = React.useState(false);

  React.useEffect(() => {
    // Control flow operations, we enforce the login-required flow, can be changes @see https://www.keycloak.org/docs/latest/securing_apps/index.html#_javascript_adapter
    keycloak
      .init({ onLoad: 'login-required' })
      .then((authenticated) => {
        setAuthenticated(authenticated);
        setInitialized(true);
      })
      .catch((err) => {
        console.error('Keycloak init error', err);
        setInitialized(true);
        setAuthenticated(false);
      });
  }, []);

  return (
    <KeycloakContext.Provider value={{ keycloak, initialized, authenticated }}>
      {children}
    </KeycloakContext.Provider>
  );
};

export const useKeycloak = () => {
  const context = React.useContext(KeycloakContext);
  if (context === undefined) {
    throw new Error('useKeycloak must be used within a KeycloakProvider');
  }
  return context;
};
