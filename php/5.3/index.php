<?php

require_once 'vendor/autoload.php';

$config = parse_ini_file('config.ini');
if (false === $config) {
    die('please you need to have a config.ini file. Make one based on config.ini.dist');
}
$clientConfig = new \fkooman\OAuth\Client\ClientConfig($config);

$tokenStorage = new \fkooman\OAuth\Client\SessionStorage();
$httpClient = new \Guzzle\Http\Client();
$api = new fkooman\OAuth\Client\Api($config['api_context'], $clientConfig, $tokenStorage, $httpClient);

$context = new \fkooman\OAuth\Client\Context($config['api_context'], explode(" ", $config['api_scopes']));

$accessToken = $api->getAccessToken($context);
if (false === $accessToken) {
    if ($_GET['authorize'] == 1) {
        header("HTTP/1.1 302 Found");
        header("Location: " . $api->getAuthorizeUri($context));
    } else {
        echo "<a href='index.php?authorize=1'>Logar com o Login do Cidadao</a>";
    }
    exit;
} else {
    try {
        $client = new \Guzzle\Http\Client();
        $bearerAuth = new \fkooman\Guzzle\Plugin\BearerAuth\BearerAuth($accessToken->getAccessToken());
        $client->addSubscriber($bearerAuth);
        $response = $client->get($config['api_url'])->send();
        header("Content-Type: application/json");
        $json = json_decode($response->getBody(), true);
        if (false !== $json) {
            print_r($json);
        } else {
            print_r($response->getBody());
        }
    } catch (\fkooman\Guzzle\Plugin\BearerAuth\Exception\BearerErrorResponseException $e) {
        if ("invalid_token" === $e->getBearerReason()) {
            // the token we used was invalid, possibly revoked, we throw it away
            $api->deleteAccessToken($context);
            $api->deleteRefreshToken($context);
            /* no valid access token available, go to authorization server */
            header("HTTP/1.1 302 Found");
            header("Location: " . $api->getAuthorizeUri($context));
            exit;
        }
        throw $e;
    } catch (\Exception $e) {
        die(sprintf('ERROR: %s', $e->getMessage()));
    }    
}

