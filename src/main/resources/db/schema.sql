CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    real_name VARCHAR(50),
    avatar_url VARCHAR(500),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_login_at TIMESTAMP,
    login_count INTEGER NOT NULL DEFAULT 0,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    refresh_token VARCHAR(500),
    refresh_token_expires_at TIMESTAMP,
    full_name VARCHAR(100),
    last_login_time TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_status ON users (status);

CREATE TABLE IF NOT EXISTS posts (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    summary VARCHAR(500),
    cover_image VARCHAR(500),
    author VARCHAR(100) NOT NULL,
    author_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    category_id BIGINT,
    view_count INTEGER NOT NULL DEFAULT 0,
    like_count INTEGER NOT NULL DEFAULT 0,
    comment_count INTEGER NOT NULL DEFAULT 0,
    display_order INTEGER DEFAULT 0,
    publish_date TIMESTAMP,
    published_at TIMESTAMP,
    tags VARCHAR(200),
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    allow_comment BOOLEAN NOT NULL DEFAULT TRUE,
    allow_comments BOOLEAN NOT NULL DEFAULT TRUE,
    seo_title VARCHAR(200),
    seo_description VARCHAR(500),
    seo_keywords VARCHAR(200)
);

CREATE INDEX IF NOT EXISTS idx_title ON posts (title);
CREATE INDEX IF NOT EXISTS idx_status_posts ON posts (status);
CREATE INDEX IF NOT EXISTS idx_publish_date ON posts (publish_date);
CREATE INDEX IF NOT EXISTS idx_category_id ON posts (category_id);
CREATE INDEX IF NOT EXISTS idx_author_id ON posts (author_id);
CREATE INDEX IF NOT EXISTS idx_featured_status ON posts (featured, status);
CREATE INDEX IF NOT EXISTS idx_status_public ON posts (status, is_public);

CREATE TABLE IF NOT EXISTS projects (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT,
    name VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    image_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNING',
    category VARCHAR(50) NOT NULL,
    start_date DATE,
    end_date DATE,
    tech_stack VARCHAR(100),
    project_url VARCHAR(100),
    github_url VARCHAR(100),
    progress INTEGER NOT NULL DEFAULT 0,
    budget DOUBLE PRECISION,
    actual_cost DOUBLE PRECISION,
    team_size INTEGER,
    leader_name VARCHAR(50),
    goals TEXT,
    achievements TEXT,
    display_order INTEGER DEFAULT 0,
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    is_public BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_project_name ON projects (name);
CREATE INDEX IF NOT EXISTS idx_status_projects ON projects (status);
CREATE INDEX IF NOT EXISTS idx_start_date ON projects (start_date);
CREATE INDEX IF NOT EXISTS idx_end_date ON projects (end_date);

CREATE TABLE IF NOT EXISTS members (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT,
    student_id VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    gender VARCHAR(10) NOT NULL,
    major VARCHAR(100),
    grade VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar_url VARCHAR(500),
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    research_direction VARCHAR(500),
    personal_profile TEXT,
    join_date DATE,
    graduation_date DATE,
    display_order INTEGER DEFAULT 0,
    linkedin_url VARCHAR(500),
    personal_website VARCHAR(500),
    bio TEXT,
    featured BOOLEAN DEFAULT FALSE,
    github_url VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_student_id ON members (student_id);
CREATE INDEX IF NOT EXISTS idx_name ON members (name);
CREATE INDEX IF NOT EXISTS idx_grade ON members (grade);
CREATE INDEX IF NOT EXISTS idx_status_members ON members (status);

CREATE TABLE IF NOT EXISTS meetings (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    meeting_date TIMESTAMP NOT NULL,
    duration_minutes INTEGER,
    location VARCHAR(200),
    host VARCHAR(50),
    participants VARCHAR(500),
    attendees VARCHAR(500),
    absentees VARCHAR(500),
    agenda TEXT,
    minutes TEXT,
    conclusion TEXT,
    action_items TEXT,
    tags VARCHAR(500),
    display_order INTEGER,
    attachment_url VARCHAR(500),
    attachment_name VARCHAR(200),
    type VARCHAR(20) DEFAULT 'REGULAR',
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    reminder_sent BOOLEAN DEFAULT FALSE,
    attendee_count INTEGER DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_title_meetings ON meetings (title);
CREATE INDEX IF NOT EXISTS idx_meeting_date ON meetings (meeting_date);
CREATE INDEX IF NOT EXISTS idx_status_meetings ON meetings (status);

CREATE TABLE IF NOT EXISTS file_uploads (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT,
    file_name VARCHAR(255) NOT NULL UNIQUE,
    original_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_type VARCHAR(255),
    file_size BIGINT,
    category VARCHAR(255),
    description VARCHAR(255),
    uploaded_by VARCHAR(255),
    uploaded_at TIMESTAMP,
    download_count INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS visit_logs (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by BIGINT,
    updated_by BIGINT,
    version BIGINT,
    user_id BIGINT,
    ip_address VARCHAR(45) NOT NULL,
    user_agent TEXT,
    url VARCHAR(500) NOT NULL,
    http_method VARCHAR(10),
    status_code INTEGER,
    response_time BIGINT,
    referer VARCHAR(500),
    traffic_source VARCHAR(50),
    page_stay_time BIGINT,
    session_id VARCHAR(100),
    device_type VARCHAR(20),
    operating_system VARCHAR(50),
    browser VARCHAR(50),
    country VARCHAR(50),
    city VARCHAR(50),
    is_new_visitor BOOLEAN,
    visit_time TIMESTAMP NOT NULL
);