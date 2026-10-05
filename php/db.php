<?php
/**
 * Back to You — Campus Lost & Found System
 * Central Database Connection (PDO)
 * Phase 4 Backend
 */

$host = '127.0.0.1';
$db   = 'back_to_you';
$user = 'root';
$pass = '';
$charset = 'utf8mb4';

$dsn = "mysql:host=$host;dbname=$db;charset=$charset";
$options = [
    PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    PDO::ATTR_EMULATE_PREPARES   => false,
];

try {
    $pdo = new PDO($dsn, $user, $pass, $options);
} catch (\PDOException $e) {
    header('Content-Type: application/json', true, 500);
    echo json_encode([
        'success' => false,
        'message' => 'Database connection failed. Please ensure MySQL is running.'
    ]);
    exit;
}
