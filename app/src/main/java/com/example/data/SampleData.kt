package com.example.data

import com.example.model.*
import java.util.UUID

object SampleDataProvider {

    fun getDefaultHackathons(userId: String): List<Hackathon> = listOf(
        Hackathon(
            id = "hack_brand_sentinel",
            userId = userId,
            name = "NexGen Tech Sprint 2026",
            organizer = "TechSprint Global Foundation",
            websiteUrl = "https://techsprint2026.example.org",
            registrationUrl = "https://techsprint2026.example.org/register",
            description = "A 48-hour global sprint building next-generation enterprise AI governance & safety tools.",
            problemStatement = "Enterprises face brand erosion from unauthorized synthetic content. Create an automated brand compliance auditor with multimodal analysis.",
            domain = "Enterprise AI & Trust",
            mode = "Hybrid",
            teamName = "SentinelForge",
            teamSize = 3,
            status = "Building",
            registrationDate = "Oct 15, 2026",
            registrationDeadline = "Nov 01, 2026",
            submissionDeadline = "Nov 14, 2026",
            finalEventDate = "Nov 16, 2026",
            prizeInfo = "$25,000 USD + Cloud Credits",
            isArchived = false
        ),
        Hackathon(
            id = "hack_carebridge",
            userId = userId,
            name = "HealthHacks International",
            organizer = "Global Health Consortium",
            websiteUrl = "https://healthhacks.example.org",
            registrationUrl = "https://healthhacks.example.org/join",
            description = "Tackling acute healthcare bottlenecks in underserved regions with low-bandwidth emergency triage.",
            problemStatement = "Ambulance rerouting delays cost lives. Build an offline-first priority routing dispatch interface for paramedic squads.",
            domain = "Healthcare & Emergency Response",
            mode = "Online",
            teamName = "CareCrew",
            teamSize = 4,
            status = "Finalist",
            registrationDate = "Sep 20, 2026",
            registrationDeadline = "Oct 10, 2026",
            submissionDeadline = "Nov 20, 2026",
            finalEventDate = "Nov 25, 2026",
            prizeInfo = "$15,000 Prize + Pilot Grant",
            isArchived = false
        ),
        Hackathon(
            id = "hack_edudrishti",
            userId = userId,
            name = "Apex AI Innovators Cup",
            organizer = "Apex University Research Labs",
            websiteUrl = "https://apexhack.example.edu",
            registrationUrl = "https://apexhack.example.edu",
            description = "National innovation cup focusing on equitable vernacular education tools for rural classrooms.",
            problemStatement = "Students in multilingual rural schools struggle with standardized STEM textbooks. Provide real-time vernacular audio tutoring.",
            domain = "EdTech & Generative AI",
            mode = "Offline",
            teamName = "VisionLabs",
            teamSize = 4,
            status = "Winner",
            registrationDate = "Aug 01, 2026",
            registrationDeadline = "Aug 25, 2026",
            submissionDeadline = "Oct 05, 2026",
            finalEventDate = "Oct 12, 2026",
            prizeInfo = "Grand Trophy + $12,000 Grant",
            isArchived = false
        ),
        Hackathon(
            id = "hack_oppkit",
            userId = userId,
            name = "DevFest Collegiate Hack",
            organizer = "Student Developer Clubs",
            websiteUrl = "https://devfest.example.com",
            registrationUrl = "https://devfest.example.com",
            description = "Campus challenge to build productivity suites for university research labs and career centers.",
            problemStatement = "Students miss research fellowships due to fragmented portal announcements. Centralize deadline discovery.",
            domain = "Student Productivity",
            mode = "Online",
            teamName = "CampusHacks",
            teamSize = 2,
            status = "Completed",
            registrationDate = "Jul 10, 2026",
            registrationDeadline = "Jul 30, 2026",
            submissionDeadline = "Aug 20, 2026",
            finalEventDate = "Aug 25, 2026",
            prizeInfo = "$5,000 Runner-Up Award",
            isArchived = true
        )
    )

    fun getDefaultTasks(userId: String): List<TaskItem> = listOf(
        TaskItem(
            id = "task_1",
            userId = userId,
            hackathonId = "hack_brand_sentinel",
            projectId = "proj_brand_sentinel",
            title = "Finalize Multimodal Brand Guard prompt schema",
            description = "Validate structured JSON output format for Gemini 3.8 Flash inference with schema constraint.",
            category = "AI/ML",
            priority = "Critical",
            status = "In Progress",
            dueDate = "Nov 11, 2026",
            assignedMember = "Arpit",
            notes = "Using responseSchema parameter in GenerationConfig for zero hallucination."
        ),
        TaskItem(
            id = "task_2",
            userId = userId,
            hackathonId = "hack_brand_sentinel",
            projectId = "proj_brand_sentinel",
            title = "Record 2-minute Loom demo walkthrough",
            description = "Screen record live anomaly detection on synthetic ad creatives.",
            category = "Pitch",
            priority = "High",
            status = "To Do",
            dueDate = "Nov 12, 2026",
            assignedMember = "Me",
            notes = "Keep intro under 20 seconds, focus on real-time metric delta."
        ),
        TaskItem(
            id = "task_3",
            userId = userId,
            hackathonId = "hack_carebridge",
            projectId = "proj_carebridge",
            title = "Prepare Finalist Grand Jury pitch deck",
            description = "10-slide deck covering problem validation, offline edge sync, and field trial metrics.",
            category = "Pitch",
            priority = "High",
            status = "In Progress",
            dueDate = "Nov 18, 2026",
            assignedMember = "Team Lead",
            notes = "Include quotes from local ambulance fleet coordinator."
        ),
        TaskItem(
            id = "task_4",
            userId = userId,
            hackathonId = "hack_brand_sentinel",
            projectId = "proj_brand_sentinel",
            title = "Clean and document public GitHub repo",
            description = "Add architecture diagram, quickstart docker-compose, and MIT license.",
            category = "Documentation",
            priority = "Medium",
            status = "Backlog",
            dueDate = "Nov 13, 2026",
            assignedMember = "Dev Lead",
            notes = "Ensure no secret tokens exist in commit history."
        ),
        TaskItem(
            id = "task_5",
            userId = userId,
            hackathonId = "hack_brand_sentinel",
            projectId = "proj_brand_sentinel",
            title = "Setup Edge-to-Edge Jetpack Compose dashboard UI",
            description = "Implement pastel purple cards, statistics grid, and responsive window sizes.",
            category = "Frontend",
            priority = "High",
            status = "Completed",
            dueDate = "Nov 09, 2026",
            assignedMember = "Arpit",
            notes = "Passed all accessibility touch target 48dp requirements."
        )
    )

    fun getDefaultProjects(userId: String): List<Project> = listOf(
        Project(
            id = "proj_brand_sentinel",
            userId = userId,
            hackathonId = "hack_brand_sentinel",
            title = "Brand Sentinel",
            description = "Autonomous brand governance and copyright anomaly detection engine powered by Gemini Multimodal AI.",
            problemStatement = "High-growth companies suffer brand dilution from counterfeit social ads that bypass traditional keyword moderation.",
            proposedSolution = "Real-time crawler analyzing visual logos, voice tonality, and claim veracity using zero-shot multimodal inference.",
            techStack = "Kotlin, Jetpack Compose, Firebase AI Logic (Gemini 3.8 Flash), Cloud Firestore",
            githubRepo = "https://github.com/hacktrack/brand-sentinel",
            liveDeployment = "https://brand-sentinel-demo.web.app",
            demoVideo = "https://youtu.be/sample-brand-sentinel",
            documentation = "https://docs.brand-sentinel.internal",
            status = "Development"
        ),
        Project(
            id = "proj_carebridge",
            userId = userId,
            hackathonId = "hack_carebridge",
            title = "CareBridge",
            description = "Mesh-networked emergency dispatch triage for remote ambulances with zero-cell-coverage fallback.",
            problemStatement = "Rural emergency medical transit encounters dead zones where GPS dispatch servers disconnect.",
            proposedSolution = "Local peer-to-peer Wi-Fi Direct sync combined with heuristic hospital ICU bed availability estimators.",
            techStack = "Android Jetpack Compose, Room DB, BLE Mesh, Google Maps SDK",
            githubRepo = "https://github.com/hacktrack/carebridge",
            liveDeployment = "https://carebridge.live",
            demoVideo = "https://youtu.be/sample-carebridge",
            documentation = "https://carebridge.live/whitepaper.pdf",
            status = "Prototype Ready"
        ),
        Project(
            id = "proj_edudrishti",
            userId = userId,
            hackathonId = "hack_edudrishti",
            title = "EduDrishti",
            description = "Vernacular AI reading companion converting textbook diagrams into tactile audio walkthroughs.",
            problemStatement = "First-generation learners in rural primary schools lack individual tutoring support in local dialects.",
            proposedSolution = "Camera snapshot to instant bilingual speech explanation with interactive quiz feedback loop.",
            techStack = "Kotlin, Gemini Multimodal, On-device TTS, Jetpack Compose",
            githubRepo = "https://github.com/hacktrack/edudrishti",
            liveDeployment = "https://edudrishti.org",
            demoVideo = "https://youtu.be/sample-edudrishti",
            documentation = "https://edudrishti.org/research",
            status = "Completed"
        )
    )

    fun getDefaultSubmissions(userId: String): List<SubmissionItem> = listOf(
        SubmissionItem(
            id = "sub_1",
            userId = userId,
            hackathonId = "hack_brand_sentinel",
            title = "Executive Summary & Architecture PDF",
            deliverableType = "Documentation",
            fileUrl = "https://storage.googleapis.com/sample/brand_sentinel_arch.pdf",
            notes = "Includes system block diagram and Firestore security rule proof.",
            status = "Completed",
            submittedAt = "Nov 08, 2026"
        ),
        SubmissionItem(
            id = "sub_2",
            userId = userId,
            hackathonId = "hack_brand_sentinel",
            title = "Working Prototype Video Demo (3 min)",
            deliverableType = "Demo Video",
            externalUrl = "https://youtu.be/brand-sentinel-pitch",
            notes = "Demonstrates automated scanning of 10 ad creatives with instant warning flags.",
            status = "In Progress",
            submittedAt = ""
        ),
        SubmissionItem(
            id = "sub_3",
            userId = userId,
            hackathonId = "hack_brand_sentinel",
            title = "Public GitHub Code Repository with README",
            deliverableType = "GitHub Repo",
            externalUrl = "https://github.com/hacktrack/brand-sentinel",
            notes = "Clean repo with setup instructions and MIT License.",
            status = "Submitted",
            submittedAt = "Nov 09, 2026"
        ),
        SubmissionItem(
            id = "sub_4",
            userId = userId,
            hackathonId = "hack_brand_sentinel",
            title = "Finalist Live Pitch Slide Deck (10 slides)",
            deliverableType = "Pitch Deck",
            fileUrl = "https://storage.googleapis.com/sample/brand_sentinel_pitch.pdf",
            notes = "Problem, TAM, Solution, Architecture, Market Traction, Team.",
            status = "Pending",
            submittedAt = ""
        )
    )

    fun getDefaultAchievements(userId: String): List<Achievement> = listOf(
        Achievement(
            id = "ach_1",
            userId = userId,
            hackathonId = "hack_edudrishti",
            competitionName = "Apex AI Innovators Cup",
            projectName = "EduDrishti",
            result = "Grand Winner",
            rank = "1st Place (out of 450+ teams)",
            date = "Oct 12, 2026",
            organizer = "Apex University Research Labs",
            certificateUrl = "https://credentials.example.org/cert/apex-2026-winner",
            proofUrl = "https://news.example.org/apex-cup-winners-announced",
            description = "Won 1st place overall and $12,000 USD grant for developing an AI vernacular reading companion for rural schools."
        ),
        Achievement(
            id = "ach_2",
            userId = userId,
            hackathonId = "hack_oppkit",
            competitionName = "DevFest Collegiate Hackathon",
            projectName = "OpportunityKit",
            result = "1st Runner-Up",
            rank = "2nd Place",
            date = "Aug 25, 2026",
            organizer = "Google Developer Student Clubs",
            certificateUrl = "https://credentials.example.org/cert/devfest-runnerup",
            proofUrl = "https://devfest.example.com/hall-of-fame",
            description = "Awarded $5,000 prize in Student Productivity category for automated research fellowship deadline aggregator."
        )
    )

    fun getDefaultTeamMembers(): List<TeamMember> = listOf(
        TeamMember(
            id = "tm_1",
            name = "Arpit (You)",
            email = "raj2010arpit@gmail.com",
            college = "Tech Institute",
            role = "Team Lead & Fullstack Engineer",
            skills = "Kotlin, Jetpack Compose, Gemini AI, System Design",
            responsibilities = "Core architecture, AI integration, and project timeline"
        ),
        TeamMember(
            id = "tm_2",
            name = "Sarah Chen",
            email = "sarah.chen@example.edu",
            college = "Design & Media College",
            role = "UI/UX Designer",
            skills = "Figma, Design Systems, Design Tokens, Micro-interactions",
            responsibilities = "User research, wireframing, pitch deck visual polish"
        ),
        TeamMember(
            id = "tm_3",
            name = "Devon Vance",
            email = "devon.v@example.edu",
            college = "School of Computing",
            role = "Backend & Cloud Engineer",
            skills = "Go, Cloud Run, Firestore, Docker, Python",
            responsibilities = "High-throughput data scrapers and API reliability"
        )
    )
}
