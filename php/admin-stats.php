<?php
/**
 * Back to You — Campus Lost & Found System
 * Admin System Overview Statistics Endpoint
 */

session_start();
header('Content-Type: application/json');
require_once __DIR__ . '/db.php';

// Authorization Check: Must be logged in AND role must be ADMIN
if (!isset($_SESSION['user_id'])) {
    header('HTTP/1.1 401 Unauthorized');
    echo json_encode(['success' => false, 'message' => 'Unauthorized. Please log in as Admin.']);
    exit;
}

if (!isset($_SESSION['role']) || $_SESSION['role'] !== 'ADMIN') {
    header('HTTP/1.1 403 Forbidden');
    echo json_encode(['success' => false, 'message' => 'Forbidden. Admin access required.']);
    exit;
}

try {
    $totalUsers     = (int)$pdo->query("SELECT COUNT(*) FROM users")->fetchColumn();
    $activeUsers    = (int)$pdo->query("SELECT COUNT(*) FROM users WHERE status = 'ACTIVE'")->fetchColumn();
    $inactiveUsers  = (int)$pdo->query("SELECT COUNT(*) FROM users WHERE status = 'INACTIVE'")->fetchColumn();
    $studentsCount  = (int)$pdo->query("SELECT COUNT(*) FROM users WHERE role = 'STUDENT'")->fetchColumn();
    $adminsCount    = (int)$pdo->query("SELECT COUNT(*) FROM users WHERE role = 'ADMIN'")->fetchColumn();
    
    $totalItems     = (int)$pdo->query("SELECT COUNT(*) FROM items")->fetchColumn();
    $totalLost      = (int)$pdo->query("SELECT COUNT(*) FROM items WHERE type = 'LOST'")->fetchColumn();
    $totalFound     = (int)$pdo->query("SELECT COUNT(*) FROM items WHERE type = 'FOUND'")->fetchColumn();
    $totalResolved  = (int)$pdo->query("SELECT COUNT(*) FROM items WHERE status = 'RESOLVED'")->fetchColumn();

    echo json_encode([
        'success' => true,
        'stats'   => [
            'total_users'    => $totalUsers,
            'active_users'   => $activeUsers,
            'inactive_users' => $inactiveUsers,
            'students_count' => $studentsCount,
            'admins_count'   => $adminsCount,
            'total_items'    => $totalItems,
            'total_lost'     => $totalLost,
            'total_found'    => $totalFound,
            'total_resolved' => $totalResolved
        ]
    ]);
} catch (\PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error while calculating stats.']);
}
