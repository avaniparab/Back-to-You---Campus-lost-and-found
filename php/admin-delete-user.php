<?php
/**
 * Back to You — Campus Lost & Found System
 * Admin Delete User Endpoint
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

if ($targetUserId <= 0) {
    echo json_encode(['success' => false, 'message' => 'Invalid user ID.']);
    exit;
}

$currentAdminId = (int)$_SESSION['user_id'];

// Protection 1: Prevent self-deletion
if ($targetUserId === $currentAdminId) {
    echo json_encode(['success' => false, 'message' => 'You cannot delete your own administrator account.']);
    exit;
}

try {
    // Check if target user exists
    $stmt = $pdo->prepare("SELECT id, role FROM users WHERE id = ?");
    $stmt->execute([$targetUserId]);
    $targetUser = $stmt->fetch();

    if (!$targetUser) {
        echo json_encode(['success' => false, 'message' => 'User not found.']);
        exit;
    }

    // Protection 2: Prevent deleting the last remaining administrator
    if ($targetUser['role'] === 'ADMIN') {
        $adminCount = (int)$pdo->query("SELECT COUNT(*) FROM users WHERE role = 'ADMIN'")->fetchColumn();
        if ($adminCount <= 1) {
            echo json_encode(['success' => false, 'message' => 'Cannot delete the only administrator account.']);
            exit;
        }
    }

    // Protection 3: Check for associated lost/found item reports
    $itemsStmt = $pdo->prepare("SELECT COUNT(*) FROM items WHERE user_id = ?");
    $itemsStmt->execute([$targetUserId]);
    $itemCount = (int)$itemsStmt->fetchColumn();

    if ($itemCount > 0) {
        echo json_encode([
            'success' => false,
            'message' => 'Cannot delete user because they have associated lost/found item reports. Deactivate the user account instead.'
        ]);
        exit;
    }

    // Delete user
    $deleteStmt = $pdo->prepare("DELETE FROM users WHERE id = ?");
    $deleteStmt->execute([$targetUserId]);

    echo json_encode([
        'success' => true,
        'message' => 'User deleted successfully.'
    ]);
} catch (\PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error while deleting user.']);
}
