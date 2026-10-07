-- V9: Discover. "Dig through the years" pages a year's stories newest first.
CREATE INDEX idx_stories_year_feed ON stories (year_of_memory, id DESC)
    WHERE status = 'VISIBLE' AND year_of_memory IS NOT NULL;

-- People search: handle prefix and display-name contains, case-insensitive.
CREATE INDEX idx_users_display_name_lower ON users (lower(display_name));
