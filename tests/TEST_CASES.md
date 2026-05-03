# 🚀 Craftistan Backend Test Suite

This directory contains the technical test specifications and automation scripts for the Craftistan backend.

## 📋 Test Scenarios

### 1. Authentication & Identity
| ID | Scenario | Verification |
|----|----------|--------------|
| AUTH-01 | User Registration | Status 200, JWT returned, User saved with ROLE_BUYER |
| AUTH-02 | Artisan Registration | Status 200, User saved with ROLE_ARTISAN |
| AUTH-03 | User Login | Status 200, Valid JWT token in response |
| AUTH-04 | Duplicate Registration | Status 400, Error message "Email already exists" |

### 2. Product Management
| ID | Scenario | Verification |
|----|----------|--------------|
| PROD-01 | Product Upload (Artisan) | Status 200, Image URL present, Translation task triggered |
| PROD-02 | Unauthorized Upload | Status 403, Buyers cannot upload products |
| PROD-03 | Update Product | Status 200, Changes reflected in DB, Re-translation triggered |

### 3. Order & Checkout
| ID | Scenario | Verification |
|----|----------|--------------|
| ORD-01 | Place Order | Status 200, Order record created, Inventory decremented |
| ORD-02 | Order Cancellation | Status 200, Status set to CANCELLED, Inventory restored |
| ORD-03 | List User Orders | Status 200, Returns only orders belonging to current user |

### 4. Wishlist & Reviews
| ID | Scenario | Verification |
|----|----------|--------------|
| WISH-01 | Add to Wishlist | Status 200, Product ID added to user's wishlist |
| WISH-02 | Remove from Wishlist | Status 200, Product ID removed from user's wishlist |
| REV-01 | Submit Review | Status 200, Rating saved, Product avg rating updated |
| REV-02 | Update Review | Status 200, Comment changed, Timestamp updated |

### 5. User Profile & Addresses
| ID | Scenario | Verification |
|----|----------|--------------|
| PROF-01 | Update Profile | Status 200, Name/Phone changed in DB |
| ADDR-01 | Add Address | Status 200, Address record created with user link |
| ADDR-02 | Set Default Address | Status 200, Only one address marked as default |

### 6. Admin Workflows
| ID | Scenario | Verification |
|----|----------|--------------|
| ADM-01 | Approve Product | Status 200, ApprovalStatus set to APPROVED, Visible to public |
| ADM-02 | Suspend User | Status 200, User isActive set to false, Token rejected |

### 7. AI & Internationalization
| ID | Scenario | Verification |
|----|----------|--------------|
| AI-01 | Async Translation | Product entity updated with JSON translations after delay |
| AI-02 | Chat Completion | Gemini API returns relevant artisan/product support response |
