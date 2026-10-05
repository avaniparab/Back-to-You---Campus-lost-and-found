<?php
/**
 * Back to You — Campus Lost & Found System
 * Session Check Endpoint
 * Phase 4 Backend
 */

session_start();
header('Content-Type: application/json');

if (isset($_SESSION['user_id'])) {
    echo json_encode([
        'logged_in' => true,
        'user'      => [
            'id'    => (int)$_SESSION['user_id'],
            'name'  => $_SESSION['name'],
            'email' => $_SESSION['email'],
            'role'  => $_SESSION['role']
        ]
    ]);
} else {
    echo json_encode([
        'logged_in' => false
    ]);
}
