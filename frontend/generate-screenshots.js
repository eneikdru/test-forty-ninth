import { chromium } from 'playwright';
import { createServer } from 'vite';
import fs from 'fs';
import path from 'path';

async function generateScreenshots() {
  const targetDir = path.resolve(process.cwd(), '../.eneik/records/design-check-bec18367-44d6-48db-ae64-37bfa6aca79e');
  if (!fs.existsSync(targetDir)) {
    fs.mkdirSync(targetDir, { recursive: true });
  }

  // Start Vite dev server
  const server = await createServer({
    configFile: path.resolve(process.cwd(), 'vite.config.js'),
    server: { port: 5173 }
  });
  await server.listen();
  console.log('Vite server running at http://localhost:5173');

  const browser = await chromium.launch();

  // Desktop 1440px
  const desktopContext = await browser.newContext({
    viewport: { width: 1440, height: 900 }
  });
  const desktopPage = await desktopContext.newPage();
  await desktopPage.goto('http://localhost:5173');
  await desktopPage.waitForSelector('[data-testid="search-input"]');
  await desktopPage.fill('[data-testid="search-input"]', 'protocol');
  await desktopPage.click('[data-testid="search-submit"]');
  await desktopPage.waitForSelector('[data-testid="document-item"]');

  const desktopPath = path.join(targetDir, 'desktop-1440.png');
  await desktopPage.screenshot({ path: desktopPath, fullPage: false });
  console.log('Saved desktop screenshot to', desktopPath);

  // Mobile 375px
  const mobileContext = await browser.newContext({
    viewport: { width: 375, height: 812 },
    isMobile: true,
    hasTouch: true,
    deviceScaleFactor: 2
  });
  const mobilePage = await mobileContext.newPage();
  await mobilePage.goto('http://localhost:5173');
  await mobilePage.waitForSelector('[data-testid="search-input"]');
  await mobilePage.fill('[data-testid="search-input"]', 'protocol');
  await mobilePage.click('[data-testid="search-submit"]');
  await mobilePage.waitForSelector('[data-testid="document-item"]');

  const mobilePath = path.join(targetDir, 'mobile-375.png');
  await mobilePage.screenshot({ path: mobilePath, fullPage: false });
  console.log('Saved mobile screenshot to', mobilePath);

  await browser.close();
  await server.close();
  console.log('Screenshot generation complete.');
}

generateScreenshots().catch((err) => {
  console.error('Error generating screenshots:', err);
  process.exit(1);
});
