# Project Prompts History

Below are all prompts, instructions, confirmations, and clarifications—explicit, short, iterative, and granular—that have been used to successfully implement code or documentation changes throughout the codebase during this project chat.

---

**Prompt 0:**
Refactor a Java Spring backend project for a Field Technician App by renaming the package from `com.example` to `com.technican` throughout all source and test files. Ensure all code, imports, and package declarations are updated accordingly. Build and run the project to verify correctness. Address any build/test failures resulting from the refactor.

---

**Prompt 1:**
yes

---

**Prompt 2:**
Continue updating all remaining files to use the correct `com.technican.restservice` package and imports, then rebuild and test the project until all errors are resolved.

---

**Prompt 3:**
Update requirements and README with the latest changes.

---

**Prompt 4:**
Read the first 40 lines of requirements.md and README.adoc.

---

**Prompt 5:**
Read lines 41-179 of requirements.md and lines 41-120 of README.adoc.

---

**Prompt 6:**
Check user registration logic in the backend to ensure it expects `roles` and persists both the user and their roles.

---

**Prompt 7:**
Inspect the user registration controller/service code for possible bugs.

---

**Prompt 8:**
Run all tests to confirm if the registration and role assignment logic is working as expected and to check for any failures related to user/role persistence.

---

**Prompt 9:**
Inspect the job assignment controller/service code for possible bugs.

---

**Prompt 10:**
Locate and update the job assignment logic to validate technician existence and role.

---

**Prompt 11:**
Add validation to assignTechnician to ensure the technician exists and has the TECHNICIAN role before assignment.

---

**Prompt 12:**
Update the controller to return a 400 error if assignment is invalid.

---

**Prompt 13:**
Run all tests to verify that the new validation for technician assignment works and that no existing functionality is broken.

---

**Prompt 14:**
Add tests to verify that assigning a job to a non-existent technician or a non-technician user returns 400 Bad Request.

---

**Prompt 15:**
Run all tests to verify the new tests for job assignment validation pass and the bug is fully covered by TDD.

---

**Prompt 16:**
Update documentation to reflect the new validation and test coverage for job assignment.

---

**Prompt 17:**
Did you create a test for the last bug?

---

**Prompt 18:**
Are there any code changes done in the past that don't have tests?

---

**Prompt 19:**
Update documentation please.

---

**Prompt 20:**
Can you look through the history of this project chat and find all prompts that have been used to successfully implement code changes throughout the codebase? Create a file named prompts.md and update it with the prompts.

---

**Prompt 21:**
That's not all prompts. What happened to the ones from the very beginning?

---

**Prompt 22:**
There are at least 10 prompts missing. Where are they?

---

yes
