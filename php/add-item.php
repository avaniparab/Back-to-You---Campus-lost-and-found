<?php
/**
 * Back to You — Campus Lost & Found System
 * Add Item Endpoint (Handles both LOST & FOUND reports)
 * Phase 4 Backend
 */

session_start();
header('Content-Type: application/json');
require_once __DIR__ . '/db.php';

// 1. Authorization Check (Must be logged in)
if (!isset($_SESSION['user_id'])) {
    header('HTTP/1.1 401 Unauthorized');
    echo json_encode(['success' => false, 'message' => 'Unauthorized. Please log in to report an item.']);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Invalid request method.']);
    exit;
}

// 2. Obtain user_id strictly from session
$userId = (int)$_SESSION['user_id'];

// Parse JSON input or $_POST
$input = json_decode(file_get_contents('php://input'), true);
if (!$input) {
    $input = $_POST;
}

$title       = isset($input['item_name']) ? trim($input['item_name']) : (isset($input['title']) ? trim($input['title']) : '');
$category    = isset($input['category']) ? trim($input['category']) : '';
$description = isset($input['description']) ? trim($input['description']) : '';
$location    = isset($input['location']) ? trim($input['location']) : '';
$date        = isset($input['date_lost']) ? trim($input['date_lost']) : (isset($input['date_found']) ? trim($input['date_found']) : (isset($input['date']) ? trim($input['date']) : ''));
$type        = isset($input['type']) ? strtoupper(trim($input['type'])) : 'LOST';
$image       = isset($input['image']) ? trim($input['image']) : null;

// 3. Server-Side Validation
if (empty($title)) {
    echo json_encode(['success' => false, 'message' => 'Item title/name is required.']);
    exit;
}

// Validate Category against allowed set
$allowedCategories = ['Electronics', 'Documents', 'Accessories', 'Books', 'Clothing', 'Stationery', 'Others'];
// Normalize category matching (case-insensitive mapping)
$matchedCategory = null;
foreach ($allowedCategories as $allowed) {
    if (strcasecmp($allowed, $category) === 0) {
        $matchedCategory = $allowed;
        break;
    }
}

if (!$matchedCategory) {
    echo json_encode(['success' => false, 'message' => 'Invalid or missing category.']);
    exit;
}

// Validate Type (MUST be LOST or FOUND)
if (!in_array($type, ['LOST', 'FOUND'], true)) {
    echo json_encode(['success' => false, 'message' => 'Invalid item report type. Must be LOST or FOUND.']);
    exit;
}

if (empty($location)) {
    echo json_encode(['success' => false, 'message' => 'Location is required.']);
    exit;
}

if (empty($date)) {
    echo json_encode(['success' => false, 'message' => 'Date is required.']);
    exit;
}

if (empty($description)) {
    echo json_encode(['success' => false, 'message' => 'Description is required.']);
    exit;
}

try {
    // 4. Insert into items table using prepared statement
    // Note: status is automatically forced to 'ACTIVE'
    $stmt = $pdo->prepare("
        INSERT INTO items (user_id, title, description, category, location, date, type, image, status)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE')
    ");
    
    $stmt->execute([
        $userId,
        $title,
        $description,
        $matchedCategory,
        $location,
        $date,
        $type,
        $image
    ]);

    $newId = $pdo->lastInsertId();

    echo json_encode([
        'success' => true,
        'message' => 'Item report submitted successfully',
        'item_id' => (int)$newId
    ]);
} catch (\PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Failed to save item report due to a database error.']);
}
