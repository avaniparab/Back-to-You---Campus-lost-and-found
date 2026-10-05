<?php
/**
 * Back to You — Campus Lost & Found System
 * Get All Items Endpoint (With optional filter support & JOIN reporter name)
 * Phase 4 Backend
 */

session_start();
header('Content-Type: application/json');
require_once __DIR__ . '/db.php';

try {
    $where = ["1=1"];
    $params = [];

    // Optional Filter: Type (LOST or FOUND)
    if (!empty($_GET['type'])) {
        $type = strtoupper(trim($_GET['type']));
        if (in_array($type, ['LOST', 'FOUND'], true)) {
            $where[] = "items.type = ?";
            $params[] = $type;
        }
    }

    // Optional Filter: Category
    if (!empty($_GET['category'])) {
        $where[] = "LOWER(items.category) = LOWER(?)";
        $params[] = trim($_GET['category']);
    }

    // Optional Filter: Status (ACTIVE or RESOLVED)
    if (!empty($_GET['status'])) {
        $status = strtoupper(trim($_GET['status']));
        if (in_array($status, ['ACTIVE', 'RESOLVED'], true)) {
            $where[] = "items.status = ?";
            $params[] = $status;
        }
    }

    // Filter: My Reports (determined ONLY from SESSION)
    if (!empty($_GET['mine'])) {
        if (isset($_SESSION['user_id'])) {
            $where[] = "items.user_id = ?";
            $params[] = (int)$_SESSION['user_id'];
        } else {
            $where[] = "1=0";
        }
    }

    // Optional Search Filter
    if (!empty($_GET['search'])) {
        $searchTerm = '%' . trim($_GET['search']) . '%';
        $where[] = "(items.title LIKE ? OR items.description LIKE ? OR items.location LIKE ? OR items.category LIKE ?)";
        $params[] = $searchTerm;
        $params[] = $searchTerm;
        $params[] = $searchTerm;
        $params[] = $searchTerm;
    }

    $whereClause = implode(' AND ', $where);

    // JOIN items and users table (NEVER select password hashes or credentials)
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
            users.email AS reporter_email
        FROM items
        JOIN users ON items.user_id = users.id
        WHERE $whereClause
        ORDER BY items.id DESC
    ";

    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    $items = $stmt->fetchAll();

    echo json_encode([
        'success' => true,
        'count'   => count($items),
        'items'   => $items
    ]);
} catch (\PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Failed to retrieve items.']);
}
