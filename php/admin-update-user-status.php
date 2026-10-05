<?php
/**
 * Back to You — Campus Lost & Found System
 * Admin Update User Status Endpoint (Activate / Deactivate)
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

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Invalid request method.']);
    exit;
}

$input = json_decode(file_get_contents('php://input'), true);
if (!$input) {
    $input = $_POST;
}

$targetUserId = isset($input['id']) ? (int)$input['id'] : 0;
$status       = isset($input['status']) ? strtoupper(trim($input['status'])) : '';

if ($targetUserId <= 0) {
    echo json_encode(['success' => false, 'message' => 'Invalid user ID.']);
    exit;
}

if (!in_array($status, ['ACTIVE', 'INACTIVE'], true)) {
    echo json_encode(['success' => false, 'message' => 'Invalid status option.']);
    exit;
}

$currentAdminId = (int)$_SESSION['user_id'];

// Protection 1: Prevent admin from deactivating their own account
if ($targetUserId === $currentAdminId && $status === 'INACTIVE') {
    echo json_encode(['success' => false, 'message' => 'You cannot deactivate your own administrator account.']);
    exit;
}

try {
    // Check if target user exists and role
    $stmt = $pdo->prepare("SELECT id, role, status FROM users WHERE id = ?");
    $stmt->execute([$targetUserId]);
    $targetUser = $stmt->fetch();

    if (!$targetUser) {
        echo json_encode(['success' => false, 'message' => 'User not found.']);
        exit;
    }

    // Protection 2: Prevent deactivating the last active administrator
    if ($targetUser['role'] === 'ADMIN' && $status === 'INACTIVE') {
        $activeAdminCount = (int)$pdo->query("SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND status = 'ACTIVE'")->fetchColumn();
        if ($activeAdminCount <= 1) {
            echo json_encode(['success' => false, 'message' => 'Cannot deactivate the only active administrator account.']);
            exit;
        }
    }

    // Perform Update
    $updateStmt = $pdo->prepare("UPDATE users SET status = ? WHERE id = ?");
    $updateStmt->execute([$status, $targetUserId]);

    echo json_encode([
        'success' => true,
        'message' => 'User status updated to ' . $status . ' successfully.'
    ]);
} catch (\PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error while updating status.']);
}
