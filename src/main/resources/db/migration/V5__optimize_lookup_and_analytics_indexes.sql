CREATE INDEX idx_urls_user_created_at ON urls(user_id, created_at DESC);
CREATE INDEX idx_urls_active_expiration ON urls(is_active, expires_at);
CREATE INDEX idx_clicks_url_clicked_at ON clicks(url_id, clicked_at DESC);
