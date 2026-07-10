-- V1: baseline del esquema de produccion (dump pg_dump 10-jul-2026, acainfodb).
-- En prod NO se ejecuta (baseline-on-migrate=true la marca como aplicada);
-- sirve para levantar BDs nuevas identicas a prod antes de V2.

--
-- PostgreSQL database dump
--

-- Dumped from database version 16.11
-- Dumped by pg_dump version 16.11

--
-- Name: email_verification_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.email_verification_tokens (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    token character varying(512) NOT NULL,
    used boolean NOT NULL,
    user_id bigint NOT NULL
);

--
-- Name: email_verification_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.email_verification_tokens_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: email_verification_tokens_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.email_verification_tokens_id_seq OWNED BY public.email_verification_tokens.id;

--
-- Name: enrollments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.enrollments (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    enrolled_at timestamp(6) without time zone NOT NULL,
    group_id bigint NOT NULL,
    price_per_hour numeric(10,2) NOT NULL,
    promoted_at timestamp(6) without time zone,
    status character varying(20) NOT NULL,
    student_id bigint NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    waiting_list_position integer,
    withdrawn_at timestamp(6) without time zone,
    approved_at timestamp(6) without time zone,
    approved_by_user_id bigint,
    rejected_at timestamp(6) without time zone,
    rejection_reason character varying(500),
    intensive_id bigint,
    CONSTRAINT enrollments_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'WAITING_LIST'::character varying, 'WITHDRAWN'::character varying, 'COMPLETED'::character varying, 'PENDING_APPROVAL'::character varying, 'REJECTED'::character varying, 'EXPIRED'::character varying])::text[])))
);

--
-- Name: enrollments_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.enrollments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: enrollments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.enrollments_id_seq OWNED BY public.enrollments.id;

--
-- Name: group_request_supporters; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.group_request_supporters (
    group_request_id bigint NOT NULL,
    supporter_id bigint
);

--
-- Name: group_requests; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.group_requests (
    id bigint NOT NULL,
    admin_response character varying(500),
    created_at timestamp(6) without time zone NOT NULL,
    created_group_id bigint,
    expires_at timestamp(6) without time zone,
    justification character varying(500),
    processed_at timestamp(6) without time zone,
    processed_by_admin_id bigint,
    requested_group_type character varying(20) NOT NULL,
    requester_id bigint NOT NULL,
    status character varying(20) NOT NULL,
    subject_id bigint NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    CONSTRAINT group_requests_requested_group_type_check CHECK (((requested_group_type)::text = ANY ((ARRAY['REGULAR_Q1'::character varying, 'INTENSIVE_Q1'::character varying, 'REGULAR_Q2'::character varying, 'INTENSIVE_Q2'::character varying])::text[]))),
    CONSTRAINT group_requests_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'EXPIRED'::character varying])::text[])))
);

--
-- Name: group_requests_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.group_requests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: group_requests_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.group_requests_id_seq OWNED BY public.group_requests.id;

--
-- Name: intensives; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.intensives (
    id bigint NOT NULL,
    capacity integer,
    created_at timestamp(6) without time zone NOT NULL,
    current_enrollment_count integer NOT NULL,
    end_date date NOT NULL,
    name character varying(80) NOT NULL,
    price_per_hour numeric(10,2),
    start_date date NOT NULL,
    status character varying(20) NOT NULL,
    subject_id bigint NOT NULL,
    teacher_id bigint NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    CONSTRAINT intensives_status_check CHECK (((status)::text = ANY ((ARRAY['OPEN'::character varying, 'CLOSED'::character varying, 'CANCELLED'::character varying])::text[])))
);

--
-- Name: intensives_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.intensives_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: intensives_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.intensives_id_seq OWNED BY public.intensives.id;

--
-- Name: materials; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.materials (
    id bigint NOT NULL,
    category character varying(50) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description character varying(1000),
    file_extension character varying(20) NOT NULL,
    file_size bigint NOT NULL,
    mime_type character varying(100) NOT NULL,
    name character varying(255) NOT NULL,
    original_filename character varying(255) NOT NULL,
    storage_path character varying(500) NOT NULL,
    stored_filename character varying(255) NOT NULL,
    subject_id bigint NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    uploaded_at timestamp(6) without time zone NOT NULL,
    uploaded_by_id bigint NOT NULL,
    download_disabled boolean DEFAULT false NOT NULL,
    download_enabled_at timestamp(6) without time zone,
    visibility_enabled_at timestamp(6) without time zone,
    visible boolean DEFAULT true NOT NULL,
    CONSTRAINT materials_category_check CHECK (((category)::text = ANY ((ARRAY['TEORIA'::character varying, 'EJERCICIOS'::character varying, 'EXAMENES'::character varying, 'PROYECTOS'::character varying, 'LABORATORIOS'::character varying, 'OTROS'::character varying])::text[])))
);

--
-- Name: materials_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.materials_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: materials_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.materials_id_seq OWNED BY public.materials.id;

--
-- Name: password_reset_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.password_reset_tokens (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    token character varying(512) NOT NULL,
    used boolean NOT NULL,
    user_id bigint NOT NULL
);

--
-- Name: password_reset_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.password_reset_tokens_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: password_reset_tokens_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.password_reset_tokens_id_seq OWNED BY public.password_reset_tokens.id;

--
-- Name: payments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payments (
    id bigint NOT NULL,
    amount numeric(10,2) NOT NULL,
    billing_month integer,
    billing_year integer,
    created_at timestamp(6) without time zone NOT NULL,
    description character varying(500),
    due_date date NOT NULL,
    enrollment_id bigint NOT NULL,
    generated_at date NOT NULL,
    paid_at timestamp(6) without time zone,
    price_per_hour numeric(8,2) NOT NULL,
    status character varying(20) NOT NULL,
    stripe_payment_intent_id character varying(100),
    student_id bigint NOT NULL,
    total_hours numeric(6,2) NOT NULL,
    type character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    CONSTRAINT payments_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'PAID'::character varying, 'CANCELLED'::character varying])::text[]))),
    CONSTRAINT payments_type_check CHECK (((type)::text = ANY ((ARRAY['INITIAL'::character varying, 'MONTHLY'::character varying, 'INTENSIVE_FULL'::character varying])::text[])))
);

--
-- Name: payments_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.payments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: payments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.payments_id_seq OWNED BY public.payments.id;

--
-- Name: refresh_tokens; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.refresh_tokens (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    revoked boolean NOT NULL,
    token character varying(512) NOT NULL,
    user_id bigint NOT NULL
);

--
-- Name: refresh_tokens_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.refresh_tokens_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: refresh_tokens_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.refresh_tokens_id_seq OWNED BY public.refresh_tokens.id;

--
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roles (
    id bigint NOT NULL,
    description character varying(255),
    type character varying(20) NOT NULL,
    CONSTRAINT roles_type_check CHECK (((type)::text = ANY ((ARRAY['ADMIN'::character varying, 'TEACHER'::character varying, 'STUDENT'::character varying])::text[])))
);

--
-- Name: roles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: roles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.roles_id_seq OWNED BY public.roles.id;

--
-- Name: schedules; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.schedules (
    id bigint NOT NULL,
    classroom character varying(20) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    day_of_week character varying(10) NOT NULL,
    end_time time(6) without time zone NOT NULL,
    group_id bigint NOT NULL,
    start_time time(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    CONSTRAINT schedules_classroom_check CHECK (((classroom)::text = ANY ((ARRAY['AULA_PORTAL1'::character varying, 'AULA_PORTAL2'::character varying, 'AULA_VIRTUAL'::character varying])::text[]))),
    CONSTRAINT schedules_day_of_week_check CHECK (((day_of_week)::text = ANY ((ARRAY['MONDAY'::character varying, 'TUESDAY'::character varying, 'WEDNESDAY'::character varying, 'THURSDAY'::character varying, 'FRIDAY'::character varying, 'SATURDAY'::character varying, 'SUNDAY'::character varying])::text[])))
);

--
-- Name: schedules_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.schedules_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: schedules_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.schedules_id_seq OWNED BY public.schedules.id;

--
-- Name: session_reservations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.session_reservations (
    id bigint NOT NULL,
    attendance_recorded_at timestamp(6) without time zone,
    attendance_recorded_by_id bigint,
    attendance_status character varying(20),
    cancelled_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone NOT NULL,
    enrollment_id bigint NOT NULL,
    mode character varying(20) NOT NULL,
    online_request_processed_at timestamp(6) without time zone,
    online_request_processed_by_id bigint,
    online_request_status character varying(20),
    online_requested_at timestamp(6) without time zone,
    reserved_at timestamp(6) without time zone NOT NULL,
    session_id bigint NOT NULL,
    status character varying(20) NOT NULL,
    student_id bigint NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    CONSTRAINT session_reservations_attendance_status_check CHECK (((attendance_status)::text = ANY ((ARRAY['PRESENT'::character varying, 'ABSENT'::character varying])::text[]))),
    CONSTRAINT session_reservations_mode_check CHECK (((mode)::text = ANY ((ARRAY['IN_PERSON'::character varying, 'ONLINE'::character varying])::text[]))),
    CONSTRAINT session_reservations_online_request_status_check CHECK (((online_request_status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[]))),
    CONSTRAINT session_reservations_status_check CHECK (((status)::text = ANY ((ARRAY['CONFIRMED'::character varying, 'CANCELLED'::character varying])::text[])))
);

--
-- Name: session_reservations_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.session_reservations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: session_reservations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.session_reservations_id_seq OWNED BY public.session_reservations.id;

--
-- Name: sessions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sessions (
    id bigint NOT NULL,
    classroom character varying(20) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    date date NOT NULL,
    end_time time(6) without time zone NOT NULL,
    group_id bigint,
    mode character varying(20) NOT NULL,
    postponed_to_date date,
    schedule_id bigint,
    start_time time(6) without time zone NOT NULL,
    status character varying(20) NOT NULL,
    subject_id bigint NOT NULL,
    type character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    intensive_id bigint,
    CONSTRAINT sessions_classroom_check CHECK (((classroom)::text = ANY ((ARRAY['AULA_PORTAL1'::character varying, 'AULA_PORTAL2'::character varying, 'AULA_VIRTUAL'::character varying])::text[]))),
    CONSTRAINT sessions_mode_check CHECK (((mode)::text = ANY ((ARRAY['IN_PERSON'::character varying, 'ONLINE'::character varying, 'DUAL'::character varying])::text[]))),
    CONSTRAINT sessions_status_check CHECK (((status)::text = ANY ((ARRAY['SCHEDULED'::character varying, 'IN_PROGRESS'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying, 'POSTPONED'::character varying])::text[]))),
    CONSTRAINT sessions_type_check CHECK (((type)::text = ANY ((ARRAY['REGULAR'::character varying, 'EXTRA'::character varying, 'SCHEDULING'::character varying])::text[])))
);

--
-- Name: sessions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sessions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: sessions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sessions_id_seq OWNED BY public.sessions.id;

--
-- Name: subject_groups; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.subject_groups (
    id bigint NOT NULL,
    capacity integer,
    created_at timestamp(6) without time zone NOT NULL,
    current_enrollment_count integer NOT NULL,
    price_per_hour numeric(10,2),
    status character varying(20) NOT NULL,
    subject_id bigint NOT NULL,
    teacher_id bigint NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    name character varying(50) NOT NULL,
    end_date date NOT NULL,
    start_date date NOT NULL,
    CONSTRAINT subject_groups_status_check CHECK (((status)::text = ANY ((ARRAY['OPEN'::character varying, 'CLOSED'::character varying, 'CANCELLED'::character varying])::text[])))
);

--
-- Name: subject_groups_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.subject_groups_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: subject_groups_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.subject_groups_id_seq OWNED BY public.subject_groups.id;

--
-- Name: subjects; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.subjects (
    id bigint NOT NULL,
    code character varying(6) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    current_group_count integer NOT NULL,
    degree character varying(50) NOT NULL,
    name character varying(100) NOT NULL,
    status character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    year integer,
    CONSTRAINT subjects_degree_check CHECK (((degree)::text = ANY ((ARRAY['INGENIERIA_INFORMATICA'::character varying, 'INGENIERIA_INDUSTRIAL'::character varying])::text[]))),
    CONSTRAINT subjects_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'ARCHIVED'::character varying])::text[])))
);

--
-- Name: subjects_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.subjects_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: subjects_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.subjects_id_seq OWNED BY public.subjects.id;

--
-- Name: terms_acceptances; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.terms_acceptances (
    id bigint NOT NULL,
    accepted_at timestamp(6) without time zone NOT NULL,
    ip_address character varying(45),
    terms_version character varying(20) NOT NULL,
    user_id bigint NOT NULL
);

--
-- Name: terms_acceptances_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.terms_acceptances_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: terms_acceptances_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.terms_acceptances_id_seq OWNED BY public.terms_acceptances.id;

--
-- Name: user_roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_roles (
    user_id bigint NOT NULL,
    role_id bigint NOT NULL
);

--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    email character varying(255) NOT NULL,
    first_name character varying(100) NOT NULL,
    last_name character varying(100) NOT NULL,
    password character varying(255) NOT NULL,
    status character varying(20) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    phone_number character varying(20),
    degree character varying(30),
    CONSTRAINT users_degree_check CHECK (((degree)::text = ANY ((ARRAY['INGENIERIA_INFORMATICA'::character varying, 'INGENIERIA_INDUSTRIAL'::character varying])::text[]))),
    CONSTRAINT users_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'BLOCKED'::character varying, 'PENDING_ACTIVATION'::character varying])::text[])))
);

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;

--
-- Name: email_verification_tokens id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.email_verification_tokens ALTER COLUMN id SET DEFAULT nextval('public.email_verification_tokens_id_seq'::regclass);

--
-- Name: enrollments id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enrollments ALTER COLUMN id SET DEFAULT nextval('public.enrollments_id_seq'::regclass);

--
-- Name: group_requests id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.group_requests ALTER COLUMN id SET DEFAULT nextval('public.group_requests_id_seq'::regclass);

--
-- Name: intensives id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.intensives ALTER COLUMN id SET DEFAULT nextval('public.intensives_id_seq'::regclass);

--
-- Name: materials id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.materials ALTER COLUMN id SET DEFAULT nextval('public.materials_id_seq'::regclass);

--
-- Name: password_reset_tokens id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens ALTER COLUMN id SET DEFAULT nextval('public.password_reset_tokens_id_seq'::regclass);

--
-- Name: payments id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments ALTER COLUMN id SET DEFAULT nextval('public.payments_id_seq'::regclass);

--
-- Name: refresh_tokens id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_tokens ALTER COLUMN id SET DEFAULT nextval('public.refresh_tokens_id_seq'::regclass);

--
-- Name: roles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles ALTER COLUMN id SET DEFAULT nextval('public.roles_id_seq'::regclass);

--
-- Name: schedules id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schedules ALTER COLUMN id SET DEFAULT nextval('public.schedules_id_seq'::regclass);

--
-- Name: session_reservations id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.session_reservations ALTER COLUMN id SET DEFAULT nextval('public.session_reservations_id_seq'::regclass);

--
-- Name: sessions id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sessions ALTER COLUMN id SET DEFAULT nextval('public.sessions_id_seq'::regclass);

--
-- Name: subject_groups id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subject_groups ALTER COLUMN id SET DEFAULT nextval('public.subject_groups_id_seq'::regclass);

--
-- Name: subjects id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subjects ALTER COLUMN id SET DEFAULT nextval('public.subjects_id_seq'::regclass);

--
-- Name: terms_acceptances id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.terms_acceptances ALTER COLUMN id SET DEFAULT nextval('public.terms_acceptances_id_seq'::regclass);

--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);

--
-- Name: email_verification_tokens email_verification_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.email_verification_tokens
    ADD CONSTRAINT email_verification_tokens_pkey PRIMARY KEY (id);

--
-- Name: enrollments enrollments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.enrollments
    ADD CONSTRAINT enrollments_pkey PRIMARY KEY (id);

--
-- Name: group_requests group_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.group_requests
    ADD CONSTRAINT group_requests_pkey PRIMARY KEY (id);

--
-- Name: intensives intensives_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.intensives
    ADD CONSTRAINT intensives_pkey PRIMARY KEY (id);

--
-- Name: materials materials_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.materials
    ADD CONSTRAINT materials_pkey PRIMARY KEY (id);

--
-- Name: password_reset_tokens password_reset_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT password_reset_tokens_pkey PRIMARY KEY (id);

--
-- Name: payments payments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (id);

--
-- Name: refresh_tokens refresh_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_pkey PRIMARY KEY (id);

--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);

--
-- Name: schedules schedules_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schedules
    ADD CONSTRAINT schedules_pkey PRIMARY KEY (id);

--
-- Name: session_reservations session_reservations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.session_reservations
    ADD CONSTRAINT session_reservations_pkey PRIMARY KEY (id);

--
-- Name: sessions sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sessions
    ADD CONSTRAINT sessions_pkey PRIMARY KEY (id);

--
-- Name: subject_groups subject_groups_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subject_groups
    ADD CONSTRAINT subject_groups_pkey PRIMARY KEY (id);

--
-- Name: subjects subjects_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subjects
    ADD CONSTRAINT subjects_pkey PRIMARY KEY (id);

--
-- Name: terms_acceptances terms_acceptances_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.terms_acceptances
    ADD CONSTRAINT terms_acceptances_pkey PRIMARY KEY (id);

--
-- Name: users uk_6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);

--
-- Name: password_reset_tokens uk_71lqwbwtklmljk3qlsugr1mig; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.password_reset_tokens
    ADD CONSTRAINT uk_71lqwbwtklmljk3qlsugr1mig UNIQUE (token);

--
-- Name: email_verification_tokens uk_ewmvysc7e9y6uy7og2c21axa9; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.email_verification_tokens
    ADD CONSTRAINT uk_ewmvysc7e9y6uy7og2c21axa9 UNIQUE (token);

--
-- Name: refresh_tokens uk_ghpmfn23vmxfu3spu3lfg4r2d; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT uk_ghpmfn23vmxfu3spu3lfg4r2d UNIQUE (token);

--
-- Name: payments uk_payment_enrollment_period; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT uk_payment_enrollment_period UNIQUE (enrollment_id, billing_month, billing_year, type);

--
-- Name: roles uk_q9npl2ty4pngm2cussiul2qj5; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT uk_q9npl2ty4pngm2cussiul2qj5 UNIQUE (type);

--
-- Name: session_reservations uk_reservation_student_session; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.session_reservations
    ADD CONSTRAINT uk_reservation_student_session UNIQUE (student_id, session_id);

--
-- Name: subjects uk_subject_code; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.subjects
    ADD CONSTRAINT uk_subject_code UNIQUE (code);

--
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id);

--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

--
-- Name: idx_enrollment_group_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_enrollment_group_id ON public.enrollments USING btree (group_id);

--
-- Name: idx_enrollment_group_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_enrollment_group_status ON public.enrollments USING btree (group_id, status);

--
-- Name: idx_enrollment_intensive_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_enrollment_intensive_id ON public.enrollments USING btree (intensive_id);

--
-- Name: idx_enrollment_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_enrollment_status ON public.enrollments USING btree (status);

--
-- Name: idx_enrollment_student_group; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_enrollment_student_group ON public.enrollments USING btree (student_id, group_id);

--
-- Name: idx_enrollment_student_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_enrollment_student_id ON public.enrollments USING btree (student_id);

--
-- Name: idx_enrollment_waiting_list; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_enrollment_waiting_list ON public.enrollments USING btree (group_id, status, waiting_list_position);

--
-- Name: idx_group_request_requester_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_group_request_requester_id ON public.group_requests USING btree (requester_id);

--
-- Name: idx_group_request_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_group_request_status ON public.group_requests USING btree (status);

--
-- Name: idx_group_request_subject_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_group_request_subject_id ON public.group_requests USING btree (subject_id);

--
-- Name: idx_group_request_subject_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_group_request_subject_status ON public.group_requests USING btree (subject_id, status);

--
-- Name: idx_group_request_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_group_request_type ON public.group_requests USING btree (requested_group_type);

--
-- Name: idx_intensive_dates; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_intensive_dates ON public.intensives USING btree (start_date, end_date);

--
-- Name: idx_intensive_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_intensive_status ON public.intensives USING btree (status);

--
-- Name: idx_intensive_subject_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_intensive_subject_id ON public.intensives USING btree (subject_id);

--
-- Name: idx_intensive_teacher_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_intensive_teacher_id ON public.intensives USING btree (teacher_id);

--
-- Name: idx_material_auto_disable; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_material_auto_disable ON public.materials USING btree (visible, download_disabled, visibility_enabled_at, download_enabled_at);

--
-- Name: idx_material_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_material_category ON public.materials USING btree (subject_id, category);

--
-- Name: idx_material_file_extension; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_material_file_extension ON public.materials USING btree (file_extension);

--
-- Name: idx_material_subject_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_material_subject_id ON public.materials USING btree (subject_id);

--
-- Name: idx_material_uploaded_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_material_uploaded_at ON public.materials USING btree (uploaded_at);

--
-- Name: idx_material_uploaded_by; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_material_uploaded_by ON public.materials USING btree (uploaded_by_id);

--
-- Name: idx_payment_billing_period; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_billing_period ON public.payments USING btree (billing_year, billing_month);

--
-- Name: idx_payment_due_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_due_date ON public.payments USING btree (due_date);

--
-- Name: idx_payment_enrollment_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_enrollment_id ON public.payments USING btree (enrollment_id);

--
-- Name: idx_payment_overdue; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_overdue ON public.payments USING btree (status, due_date);

--
-- Name: idx_payment_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_status ON public.payments USING btree (status);

--
-- Name: idx_payment_student_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_student_id ON public.payments USING btree (student_id);

--
-- Name: idx_payment_student_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_student_status ON public.payments USING btree (student_id, status);

--
-- Name: idx_payment_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_type ON public.payments USING btree (type);

--
-- Name: idx_reservation_attendance; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reservation_attendance ON public.session_reservations USING btree (attendance_status);

--
-- Name: idx_reservation_enrollment_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reservation_enrollment_id ON public.session_reservations USING btree (enrollment_id);

--
-- Name: idx_reservation_mode; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reservation_mode ON public.session_reservations USING btree (mode);

--
-- Name: idx_reservation_online_request; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reservation_online_request ON public.session_reservations USING btree (online_request_status);

--
-- Name: idx_reservation_session_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reservation_session_id ON public.session_reservations USING btree (session_id);

--
-- Name: idx_reservation_session_status_mode; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reservation_session_status_mode ON public.session_reservations USING btree (session_id, status, mode);

--
-- Name: idx_reservation_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reservation_status ON public.session_reservations USING btree (status);

--
-- Name: idx_reservation_student_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reservation_student_id ON public.session_reservations USING btree (student_id);

--
-- Name: idx_reservation_student_session; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reservation_student_session ON public.session_reservations USING btree (student_id, session_id);

--
-- Name: idx_schedule_classroom; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_schedule_classroom ON public.schedules USING btree (classroom);

--
-- Name: idx_schedule_conflict_check; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_schedule_conflict_check ON public.schedules USING btree (classroom, day_of_week, start_time, end_time);

--
-- Name: idx_schedule_day_of_week; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_schedule_day_of_week ON public.schedules USING btree (day_of_week);

--
-- Name: idx_schedule_group_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_schedule_group_id ON public.schedules USING btree (group_id);

--
-- Name: idx_session_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_session_date ON public.sessions USING btree (date);

--
-- Name: idx_session_group_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_session_group_date ON public.sessions USING btree (group_id, date);

--
-- Name: idx_session_group_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_session_group_id ON public.sessions USING btree (group_id);

--
-- Name: idx_session_intensive_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_session_intensive_date ON public.sessions USING btree (intensive_id, date);

--
-- Name: idx_session_intensive_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_session_intensive_id ON public.sessions USING btree (intensive_id);

--
-- Name: idx_session_schedule_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_session_schedule_date ON public.sessions USING btree (schedule_id, date);

--
-- Name: idx_session_schedule_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_session_schedule_id ON public.sessions USING btree (schedule_id);

--
-- Name: idx_session_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_session_status ON public.sessions USING btree (status);

--
-- Name: idx_session_subject_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_session_subject_id ON public.sessions USING btree (subject_id);

--
-- Name: idx_session_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_session_type ON public.sessions USING btree (type);

--
-- Name: idx_subject_group_dates; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_subject_group_dates ON public.subject_groups USING btree (start_date, end_date);

--
-- Name: idx_subject_group_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_subject_group_status ON public.subject_groups USING btree (status);

--
-- Name: idx_subject_group_subject_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_subject_group_subject_id ON public.subject_groups USING btree (subject_id);

--
-- Name: idx_subject_group_teacher_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_subject_group_teacher_id ON public.subject_groups USING btree (teacher_id);

--
-- Name: uk_enrollment_student_group_active_states; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_enrollment_student_group_active_states ON public.enrollments USING btree (student_id, group_id) WHERE ((status)::text = ANY ((ARRAY['PENDING_APPROVAL'::character varying, 'ACTIVE'::character varying, 'WAITING_LIST'::character varying])::text[]));

--
-- Name: user_roles fkh8ciramu9cc9q3qcqiv4ue8a6; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkh8ciramu9cc9q3qcqiv4ue8a6 FOREIGN KEY (role_id) REFERENCES public.roles(id);

--
-- Name: group_request_supporters fkh9wq8img9emw7xb7hh7m2sp8x; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.group_request_supporters
    ADD CONSTRAINT fkh9wq8img9emw7xb7hh7m2sp8x FOREIGN KEY (group_request_id) REFERENCES public.group_requests(id);

--
-- Name: user_roles fkhfh9dx7w3ubf1co1vdev94g3f; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkhfh9dx7w3ubf1co1vdev94g3f FOREIGN KEY (user_id) REFERENCES public.users(id);

--
-- PostgreSQL database dump complete
--
