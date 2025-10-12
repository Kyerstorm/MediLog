# Known Bugs

## 🐛 Active Bugs

### 1. Discontinued Medications Toggle Not Working
**Status:** ❌ UNRESOLVED
**Date Reported:** October 11, 2025
**Severity:** Medium

**Description:**
The eye icon toggle for showing/hiding discontinued medications in the Medications tab does not respond when clicked. The icon is visible with a badge showing the count of discontinued medications, but clicking it does not show/hide the discontinued medications section.

**Expected Behavior:**
- Click eye icon (VisibilityOff) → Icon changes to Visibility → Discontinued medications section appears below active medications
- Click again → Icon changes back → Discontinued medications section hides

**Actual Behavior:**
- Click has no visible effect
- State may be changing but UI is not recomposing
- No visual feedback to user

**Implementation Attempted:**
- Added `remember { mutableStateOf(false) }` for `showDiscontinued` state
- Added badge with count to eye icon
- Added color coding (primary when shown, gray when hidden)
- Added debug logging
- Added prominent divider and visual separator
- Conditional rendering: `if (showDiscontinued && discontinuedMedications.isNotEmpty())`

**Possible Causes:**
1. Compose recomposition not triggering when `showDiscontinued` changes
2. State being reset/lost somehow
3. UI update cycle issue with LazyColumn items
4. Need to use `derivedStateOf` or different state management approach

**Files Modified:**
- `app/src/main/java/com/healthcalendar/app/ui/screens/medication/MedicationListScreen.kt`

**Next Steps to Investigate:**
- [ ] Check if onClick is actually being called (add Toast notification)
- [ ] Try using ViewModel to manage state instead of local remember
- [ ] Check if issue is specific to LazyColumn items
- [ ] Try simplified version with just Text showing state value
- [ ] Verify Compose version and check for known issues

**Workaround:**
None currently. Discontinued medications are always hidden by default, which was the main requirement. Users cannot toggle visibility.

---

## 🐛 Recently Fixed Bugs

### 1. Share Button Crash (Scanned Documents)
**Status:** ✅ FIXED
**Date Fixed:** October 11, 2025

**Fix:** Added try-catch error handling to prevent crashes and show user-friendly error message

---

## 📝 Feature Requests

See `FEATURE_IMPLEMENTATION_STATUS.md` for pending feature requests.
