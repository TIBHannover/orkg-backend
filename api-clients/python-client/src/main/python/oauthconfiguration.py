import time
from typing import Optional, Dict, Union

from keycloak import KeycloakAuthenticationError, KeycloakOpenID, KeycloakOperationError
from orkg_client import Configuration
from orkg_client.configuration import AuthSettings, ServerVariablesT, BearerFormatAuthSetting
from typing_extensions import override


class OAuth2Configuration(Configuration):
    @override
    def __init__(
        self,
        host: Optional[str]=None,
        api_key: Optional[Dict[str, str]]=None,
        api_key_prefix: Optional[Dict[str, str]]=None,
        username: Optional[str]=None,
        password: Optional[str]=None,
        access_token: Optional[str]=None,
        server_index: Optional[int]=None,
        server_variables: Optional[ServerVariablesT]=None,
        server_operation_index: Optional[Dict[int, int]]=None,
        server_operation_variables: Optional[Dict[int, ServerVariablesT]]=None,
        ignore_operation_servers: bool=False,
        ssl_ca_cert: Optional[str]=None,
        retries: Optional[int] = None,
        ca_cert_data: Optional[Union[str, bytes]] = None,
        cert_file: Optional[str]=None,
        key_file: Optional[str]=None,
        *,
        debug: Optional[bool] = None,
        auth_host: Optional[str]=None,
        auth_client_id: Optional[str]=None,
        auth_client_secret: Optional[str]=None,
        auth_realm: Optional[str]=None,
    ) -> None:
        super(OAuth2Configuration, self).__init__(
            host=host,
            api_key=api_key,
            api_key_prefix=api_key_prefix,
            username=username,
            password=password,
            access_token=access_token,
            server_index=server_index,
            server_variables=server_variables,
            server_operation_index=server_operation_index,
            server_operation_variables=server_operation_variables,
            ignore_operation_servers=ignore_operation_servers,
            ssl_ca_cert=ssl_ca_cert,
            retries=retries,
            ca_cert_data=ca_cert_data,
            cert_file=cert_file,
            key_file=key_file,
            debug=debug,
        )
        self.keycloak_openid = KeycloakOpenID(
            server_url=self._base_path if auth_host is None else auth_host,
            client_id="orkg-client" if auth_client_id is None else auth_client_id,
            client_secret_key="**********" if auth_client_secret is None else auth_client_secret,
            realm_name="orkg" if auth_realm is None else auth_realm,
        )
        self._login(time.time())

    @override
    def auth_settings(self) -> AuthSettings:
        auth: AuthSettings = {}
        access_token = self._get_access_token()
        if access_token is not None:
            auth['bearerAuthJWT'] = BearerFormatAuthSetting(
                **{
                    'type': 'bearer',
                    'in': 'header',
                    'format': 'JWT',
                    'key': 'Authorization',
                    'value': 'Bearer ' + access_token
                }
            )
        return auth

    def _get_access_token(self) -> str:
        timestamp = time.time()
        if self.timestamp + self.jwt.get("expires_in") < timestamp:
            if self.timestamp + self.jwt.get("refresh_expires_in") < timestamp:
                self.logger.debug("Access token expired. Signing in using credentials...")
                self._login(timestamp)
            else:
                self.logger.debug(
                    "Access token expired. Using refresh token to refresh access token."
                )
                self._refresh_token(timestamp)
        return self.jwt.get("access_token")

    def _login(self, timestamp: float):
        self.jwt = self.keycloak_openid.token(
            grant_type="password",
            username=self.username,
            password=self.password,
        )
        self.timestamp = timestamp

    def _refresh_token(self, timestamp: float):
        try:
            refresh_token = self.jwt.get("refresh_token")
            self.jwt = self.keycloak_openid.refresh_token(refresh_token)
            self.timestamp = timestamp
        except (KeycloakAuthenticationError, KeycloakOperationError):
            self.logger.debug("Access token refresh failed. Signing in using credentials...")
            self._login(timestamp)
