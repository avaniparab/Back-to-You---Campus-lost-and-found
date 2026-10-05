<?php
/**
 * Back to You — Campus Lost & Found System
 * Delete Item Endpoint (Owner authorization check)
 * Phase 4 Backend
 */

session_start();
header('Content-Type: application/json');
require_once __DIR__ . '/db.php';

// 1. Authorization Check
if (!isset($_SESSION['user_id'])) {
    header('HTTP/1.1 401 Unauthorized');
    echo json_encode(['success' => false, 'message' => 'Unauthorized. Please log in.']);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST' && $_SERVER['REQUEST_METHOD'] !== 'DELETE') {
    echo json_encode(['success' => false, 'message' => 'Invalid request method.']);
    exit;
}

$input = json_decode(file_get_contents('php://input'), true);
if (!$input) {
    $input = $_POST;
}

$itemId = isset($input['id']) ? (int)$input['id'] : (isset($_GET['id']) ? (int)$_GET['id'] : 0);

if ($itemId <= 0) {
    echo json_encode(['success' => false, 'message' => 'Invalid item ID.']);
    exit;
}

try {
    // 2. Check item existence & ownership
    $stmt = $pdo->prepare("SELECT user_id FROM items WHERE id = ?");
    $stmt->execute([$itemId]);
    $item = $stmt->fetch();

    if (!$item) {
        echo json_encode(['success' => false, 'message' => 'Item not found.']);
        exit;
    }

    $sessionUserId = (int)$_SESSION['user_id'];
    $sessionRole   = isset($_SESSION['role']) ? $_SESSION['role'] : 'STUDENT';

    // Verify ownership OR Admin privilege
    if ((int)$item['user_id'] !== $sessionUserId && $sessionRole !== 'ADMIN') {
        header('HTTP/1.1 403 Forbidden');
        echo json_encode(['success' => false, 'message' => 'Forbidden. You can only delete your own item reports.']);
        exit;
    }

    // 3. Delete item using prepared statement
    $delStmt = $pdo->prepare("DELETE FROM items WHERE id = ?");
    $delStmt->execute([$itemId]);

    echo json_encode([
        'success' => true,
        'message' => 'Item deleted successfully.'
    ]);
} catch (\PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Failed to delete item due to a database error.']);
}
