<?php

require_once 'vendor/autoload.php';

$config = parse_ini_file('config.ini');
if (false === $config) {
    die('please you need to have a config.ini file. Make one based on config.ini.dist');
}
$clientConfig = new \fkooman\OAuth\Client\ClientConfig($config);

try {
    $tokenStorage = new \fkooman\OAuth\Client\SessionStorage();
    $httpClient = new \Guzzle\Http\Client();
    $cb = new \fkooman\OAuth\Client\Callback($config['api_context'], $clientConfig, $tokenStorage, $httpClient);
    $cb->handleCallback($_GET);

    header("HTTP/1.1 302 Found");
    header("Location: index.php");
    exit;
} catch (\fkooman\OAuth\Client\AuthorizeException $e) {
    // this exception is thrown by Callback when the OAuth server returns a
    // specific error message for the client, e.g.: the user did not authorize
    // the request
    die(sprintf("ERROR: %s, DESCRIPTION: %s", $e->getMessage(), $e->getDescription()));
} catch (\Exception $e) {
    // other error, these should never occur in the normal flow
    die(sprintf("ERROR: %s", $e->getMessage()));
}
