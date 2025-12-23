# CurseForge & Modrinth Upload Guide

This guide provides step-by-step instructions for uploading screenshots and project icon to CurseForge and Modrinth.

---

## Prerequisites

### Required Files

All files are located in this repository:

#### Project Icon
- `assets/icon.png` (512x512px, 44 KB)

#### Screenshots
- **Featured Image**: `docs/screenshots/00-featured-dimension-overview.png` (1920x1080, 4.73 MiB)
- **Gallery Images** (8 files):
  - `docs/screenshots/01-ancient-ruins.png` (5.0 MiB)
  - `docs/screenshots/02-portal-activation.png` (3.9 MiB)
  - `docs/screenshots/03-chrono-dawn-dimension.png` (4.5 MiB)
  - `docs/screenshots/04-forgotten-library.png` (4.0 MiB)
  - `docs/screenshots/05-time-guardian-battle.png` (3.8 MiB)
  - `docs/screenshots/06-temporal-phantom.png` (4.0 MiB)
  - `docs/screenshots/07-time-tyrant-battle.png` (3.5 MiB)
  - `docs/screenshots/08-ultimate-artifacts.png` (4.4 MiB)

#### Description Files
- `docs/curseforge_description.md`
- `docs/modrinth_description.md`

---

## CurseForge Upload (T516)

### Step 1: Access Project Settings

1. Log in to [CurseForge](https://www.curseforge.com/)
2. Navigate to your project: **Chrono Dawn**
3. Click **"Manage Project"** or **"Settings"**

### Step 2: Upload Project Icon

1. Go to **"Project Settings"** or **"General"** tab
2. Find **"Project Icon"** or **"Logo"** section
3. Click **"Upload"** or **"Choose File"**
4. Select: `assets/icon.png`
5. Crop/adjust if needed (should be 512x512, centered)
6. Click **"Save"**

**Expected Result**: Icon appears on project page and in search results

### Step 3: Upload Featured Image

1. Go to **"Media"** or **"Gallery"** tab
2. Find **"Featured Image"** or **"Hero Image"** section
3. Click **"Upload Featured Image"**
4. Select: `docs/screenshots/00-featured-dimension-overview.png`
5. Click **"Save"** or **"Set as Featured"**

**Expected Result**: Large hero image appears at top of project page

### Step 4: Upload Gallery Screenshots

1. Stay in **"Media"** or **"Gallery"** tab
2. Find **"Gallery"** or **"Screenshots"** section
3. Click **"Add Screenshot"** or **"Upload"**
4. Upload screenshots **in order**:
   - `01-ancient-ruins.png`
   - `02-portal-activation.png`
   - `03-chrono-dawn-dimension.png`
   - `04-forgotten-library.png`
   - `05-time-guardian-battle.png`
   - `06-temporal-phantom.png`
   - `07-time-tyrant-battle.png`
   - `08-ultimate-artifacts.png`

5. **Optional**: Add captions for each screenshot:
   - 01: "Ancient Ruins structure in the Overworld"
   - 02: "Chrono Dawn portal activation"
   - 03: "Chrono Dawn dimension overview"
   - 04: "Forgotten Library interior"
   - 05: "Time Guardian boss battle"
   - 06: "Temporal Phantom in Phantom Catacombs"
   - 07: "Time Tyrant boss battle in Master Clock"
   - 08: "Ultimate artifacts showcase"

6. Click **"Save"** or **"Done"**

**Expected Result**: Gallery appears on project page with 8 screenshots

### Step 5: Update Description (Optional)

If the description is not already set:

1. Go to **"Description"** tab
2. Copy contents from `docs/curseforge_description.md`
3. Paste into description editor
4. Preview to ensure formatting is correct
5. Click **"Save"**

### Step 6: Verify Upload

1. View your project page (public view)
2. Check:
   - ✅ Project icon appears in header
   - ✅ Featured image appears as hero image
   - ✅ Gallery contains 8 screenshots in correct order
   - ✅ Description includes "📷 Screenshots" section with Gallery reference

---

## Modrinth Upload (T517)

### Step 1: Access Project Settings

1. Log in to [Modrinth](https://modrinth.com/)
2. Navigate to your project: **Chrono Dawn**
3. Click **"Settings"** or **"Edit"**

### Step 2: Upload Project Icon

1. Go to **"General"** or **"Project Settings"** tab
2. Find **"Icon"** section
3. Click **"Upload Icon"**
4. Select: `assets/icon.png`
5. Adjust crop if needed (512x512, centered)
6. Click **"Save Changes"**

**Expected Result**: Icon appears on project card and page

### Step 3: Upload Gallery Images

Modrinth typically combines featured image and gallery into one section.

1. Go to **"Gallery"** tab
2. Click **"Upload Image"** or **"Add Image"**
3. Upload **featured image first**:
   - Select: `docs/screenshots/00-featured-dimension-overview.png`
   - Check **"Featured"** or **"Set as Featured"**
   - **Optional Title**: "Chrono Dawn Dimension Overview"
   - **Optional Description**: "Desert Clock Tower and dimension landscape"
   - Click **"Upload"** or **"Add"**

4. Upload **gallery screenshots** in order:
   - `01-ancient-ruins.png` → Title: "Ancient Ruins (Overworld)"
   - `02-portal-activation.png` → Title: "Portal Activation"
   - `03-chrono-dawn-dimension.png` → Title: "Dimension Overview"
   - `04-forgotten-library.png` → Title: "Forgotten Library"
   - `05-time-guardian-battle.png` → Title: "Time Guardian Battle"
   - `06-temporal-phantom.png` → Title: "Temporal Phantom"
   - `07-time-tyrant-battle.png` → Title: "Time Tyrant Battle"
   - `08-ultimate-artifacts.png` → Title: "Ultimate Artifacts"

5. Arrange order if needed (drag and drop)
6. Click **"Save"**

**Expected Result**: Gallery appears with 9 images (1 featured + 8 gallery)

### Step 4: Update Description (Optional)

If the description is not already set:

1. Go to **"Description"** tab
2. Copy contents from `docs/modrinth_description.md`
3. Paste into description editor (Markdown supported)
4. Preview to ensure formatting is correct
5. Click **"Save Changes"**

### Step 5: Verify Upload

1. View your project page
2. Check:
   - ✅ Project icon appears
   - ✅ Featured image appears as hero/banner
   - ✅ Gallery contains 9 images total
   - ✅ Description renders correctly

---

## File Size Verification

Before uploading, verify file sizes are within limits:

**CurseForge**:
- Icon: No specific limit (512x512 recommended)
- Screenshots: Typically <10 MiB per image

**Modrinth**:
- Icon: <256 KB recommended
- Screenshots: <5 MiB per image ✅ **All our files are within this limit**

**Current File Sizes**:
- Icon: 44 KB ✅
- Featured: 4.73 MiB ✅
- Screenshots: 3.38-5.0 MiB ✅

All files are within limits for both platforms.

---

## Troubleshooting

### Upload Failed - File Too Large
- **Solution**: Use ImageMagick to compress (should not be needed)
- **Command**: `magick input.png -quality 90 output.png`

### Image Not Displaying
- **Check**: File format is PNG (not JPEG)
- **Check**: Resolution is 1920x1080
- **Clear**: Browser cache and reload page

### Featured Image Not Setting
- **CurseForge**: Ensure "Set as Featured" checkbox is checked
- **Modrinth**: First image in gallery is automatically featured

### Description Formatting Issues
- **Check**: Markdown syntax is correct
- **Use**: Platform's preview feature
- **Note**: Some markdown features may differ between platforms

---

## Completion Checklist

### CurseForge (T516)
- [ ] Project icon uploaded and visible
- [ ] Featured image (00) uploaded and set as hero
- [ ] Gallery images (01-08) uploaded in correct order
- [ ] Optional: Captions added to gallery images
- [ ] Description updated (if needed)
- [ ] Public page verified

### Modrinth (T517)
- [ ] Project icon uploaded and visible
- [ ] Featured image (00) uploaded and marked as featured
- [ ] Gallery images (01-08) uploaded in correct order
- [ ] Optional: Titles/descriptions added to gallery images
- [ ] Description updated (if needed)
- [ ] Public page verified

---

## Post-Upload Tasks (T518)

After uploading to both platforms:

### Update Repository Documentation

1. Update `README.md`:
   - Add actual CurseForge and Modrinth URLs
   - Update download links
   - Add badges (optional)

2. Update `docs/curseforge_description.md`:
   - Update Modrinth link (line 192)

3. Update `docs/modrinth_description.md`:
   - Update CurseForge link (if needed)

### Update tasks.md

Mark the following tasks as completed:
- T511: Set up screenshot infrastructure ✅
- T512: Create project icon ✅
- T513: Capture essential screenshots ✅
- T514: Capture supplementary screenshots ✅ (partial - featured image only)
- T515: Optimize and organize screenshots ✅
- T516: Upload to CurseForge gallery
- T517: Upload to Modrinth gallery
- T518: Update documentation with screenshots

---

## Notes

- **Manual Process**: Screenshot uploads must be done manually via web UI
- **Order Matters**: Upload gallery images in numeric order for best presentation
- **Captions Optional**: Titles/descriptions help users understand each screenshot
- **Featured Image**: Most important - appears in search results and project header
- **File Naming**: Keep original filenames for reference, but platforms don't display them

---

**Estimated Time**: 30-45 minutes total (15-20 minutes per platform)

**Priority**: High - Required for initial release approval on both platforms
