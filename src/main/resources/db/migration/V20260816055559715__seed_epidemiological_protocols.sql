CREATE TABLE epidemiological_protocols (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(512) NOT NULL,
    category VARCHAR(100) NOT NULL,
    version VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    summary TEXT,
    author_organization VARCHAR(256) NOT NULL,
    publication_year INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_epi_protocols_category ON epidemiological_protocols (category);
CREATE INDEX idx_epi_protocols_code ON epidemiological_protocols (code);

INSERT INTO epidemiological_protocols (code, title, category, version, status, summary, author_organization, publication_year)
VALUES
('EPI-PROTO-001', 'COVID-19 Public Health Surveillance and Outbreak Investigation Protocol', 'Respiratory', 'v3.2', 'APPROVED', 'Comprehensive guidance for standard case definitions, contact tracing, and outbreak investigation protocols for SARS-CoV-2.', 'World Health Organization', 2022),
('EPI-PROTO-002', 'Cholera Outbreak Early Warning and Rapid Response Protocol', 'Enteric', 'v2.1', 'APPROVED', 'Standard procedures for cholera case detection, water source testing, oral cholera vaccine deployment, and epidemic control.', 'CDC Epidemic Intelligence Service', 2021),
('EPI-PROTO-003', 'Global Influenza Sentinel Surveillance Operating Procedure', 'Respiratory', 'v4.0', 'APPROVED', 'Methodology for ILI and SARI sentinel surveillance, specimen collection, and genetic characterization protocols.', 'Global Influenza Surveillance Network', 2023),
('EPI-PROTO-004', 'Measles Outbreak Containment and Contact Tracing Standard', 'Vaccine-Preventable', 'v2.0', 'APPROVED', 'Rapid investigation response protocol for suspected measles clusters including post-exposure prophylaxis and ring vaccination guidelines.', 'ECDC', 2020),
('EPI-PROTO-005', 'Tuberculosis Active Case Finding and Contact Screening Protocol', 'Mycobacterial', 'v1.5', 'APPROVED', 'Systematic screening standards for household and community contacts of pulmonary tuberculosis patients using chest X-ray and IGRA/TST.', 'Stop TB Partnership', 2021),
('EPI-PROTO-006', 'Dengue Fever Vector Surveillance and Outbreak Response Framework', 'Vector-Borne', 'v3.0', 'APPROVED', 'Integrated vector management, larval index monitoring, and clinical triage response guidelines during seasonal dengue surges.', 'Pan American Health Organization', 2022),
('EPI-PROTO-007', 'Ebola Virus Disease IPC and Ring Vaccination Field Protocol', 'Viral Hemorrhagic Fever', 'v2.4', 'APPROVED', 'Infection prevention, quarantine protocols, and ring vaccination tracking procedures for Filovirus outbreaks.', 'African CDC', 2021),
('EPI-PROTO-008', 'Malaria Early Warning and Epidemic Preparedness Operating Standard', 'Vector-Borne', 'v1.8', 'APPROVED', 'Climatic anomaly monitoring, rapid diagnostic test (RDT) deployment, and mass drug administration triggers for malaria epidemics.', 'Roll Back Malaria Partnership', 2020),
('EPI-PROTO-009', 'Hospital-Acquired AMR Surveillance and Containment Protocol', 'Antimicrobial Resistance', 'v2.2', 'APPROVED', 'Facility-wide monitoring guidelines for CRE and MRSA pathogens, antimicrobial stewardship metrics, and isolation protocols.', 'Global AMR Surveillance System', 2023),
('EPI-PROTO-010', 'Mpox Outbreak Contact Tracing and Laboratory Diagnostic Standard', 'Zoonotic', 'v1.1', 'APPROVED', 'Standardized case definitions, PCR swab collection procedures, contact monitoring for 21 days, and smallpox vaccine deployment.', 'World Health Organization', 2022);
