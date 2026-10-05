<?php
/**
 * Back to You — Campus Lost & Found System
 * Get Single Item Endpoint
 * Phase 4 Backend
 */

header('Content-Type: application/json');
require_once __DIR__ . '/db.php';

$id = isset($_GET['id']) ? (int)$_GET['id'] : 0;

if ($id <= 0) {
    echo json_encode(['success' => false, 'message' => 'Invalid item ID.']);
    exit;
}

try {
    $sql = "
        SELECT 
            items.id,
            items.user_id,
            items.title,
            items.description,
            items.category,
            items.location,
            items.date,
            items.type,
            items.image,
            items.status,
            items.created_at,
            users.name AS reporter_name,
            users.email AS reporter_email,
            users.role AS reporter_role
        FROM items
        JOIN users ON items.user_id = users.id
        WHERE items.id = ?
        LIMIT 1
    ";

    $stmt = $pdo->prepare($sql);
    $stmt->execute([$id]);
    $item = $stmt->fetch();

    if (!$item) {
        echo json_encode(['success' => false, 'message' => 'Item not found']);
        exit;
    }

    echo json_encode([
        'success' => true,
        'item'    => $item
    ]);
} catch (\PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error while fetching item details.']);
}
