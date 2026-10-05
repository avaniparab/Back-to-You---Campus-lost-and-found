<?php
/**
 * Back to You — Campus Lost & Found System
 * Update Item Endpoint (Status update / detail editing)
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

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Invalid request method.']);
    exit;
}

$input = json_decode(file_get_contents('php://input'), true);
if (!$input) {
    $input = $_POST;
}

$itemId = isset($input['id']) ? (int)$input['id'] : 0;
if ($itemId <= 0) {
    echo json_encode(['success' => false, 'message' => 'Invalid or missing item ID.']);
    exit;
}

try {
    // 2. Fetch existing item to check ownership
    $stmt = $pdo->prepare("SELECT user_id, title, description, category, location, date, status FROM items WHERE id = ?");
    $stmt->execute([$itemId]);
    $item = $stmt->fetch();

    if (!$item) {
        echo json_encode(['success' => false, 'message' => 'Item not found.']);
        exit;
    }

    // Verify ownership OR Admin privilege
    $sessionUserId = (int)$_SESSION['user_id'];
    $sessionRole   = isset($_SESSION['role']) ? $_SESSION['role'] : 'STUDENT';

    if ((int)$item['user_id'] !== $sessionUserId && $sessionRole !== 'ADMIN') {
        header('HTTP/1.1 403 Forbidden');
        echo json_encode(['success' => false, 'message' => 'Forbidden. You do not have permission to edit this item.']);
        exit;
    }

    // 3. Prepare updated values (falling back to existing values if not provided)
    $title       = isset($input['title']) ? trim($input['title']) : $item['title'];
    $description = isset($input['description']) ? trim($input['description']) : $item['description'];
    $category    = isset($input['category']) ? trim($input['category']) : $item['category'];
    $location    = isset($input['location']) ? trim($input['location']) : $item['location'];
    $date        = isset($input['date']) ? trim($input['date']) : $item['date'];
    $status      = isset($input['status']) ? strtoupper(trim($input['status'])) : $item['status'];

    if (!in_array($status, ['ACTIVE', 'RESOLVED'], true)) {
        echo json_encode(['success' => false, 'message' => 'Invalid status value. Must be ACTIVE or RESOLVED.']);
        exit;
    }

    $updateStmt = $pdo->prepare("
        UPDATE items 
        SET title = ?, description = ?, category = ?, location = ?, date = ?, status = ?
        WHERE id = ?
    ");

    $updateStmt->execute([
        $title,
        $description,
        $category,
        $location,
        $date,
        $status,
        $itemId
    ]);

    echo json_encode([
        'success' => true,
        'message' => 'Item updated successfully.'
    ]);
} catch (\PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Failed to update item due to a database error.']);
}
