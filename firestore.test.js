const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_001";
const BOB_UID = "bob_uid_002";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProjectPayload(uid, createdAt) {
  return {
    userId: uid,
    title: "Kyoto Cherry Blossoms",
    schemaVersion: 1,
    layoutType: "BENTO",
    photoCount: 4,
    favorite: false,
    archived: false,
    revision: 1,
    canvas: {
      width: 3000,
      height: 2000,
      unit: "PX",
      dpi: 300,
      aspectLocked: true,
      resizeMode: "FIT",
      orientation: "LANDSCAPE",
    },
    background: {
      type: "SOLID",
      primaryColor: -1,
      secondaryColor: -135698,
      textureIntensity: 0.0,
    },
    border: {
      cornerRadius: 16.0,
      spacing: 8.0,
      padding: 12.0,
      color: -1,
      shadowEnabled: false,
    },
    layerSummaries: [
      {
        layerId: "layer_1",
        layerType: "PHOTO",
        zIndex: 0,
        label: "Pagoda",
      },
    ],
    createdAt,
    updatedAt: createdAt,
  };
}

function validExportPayload(uid, createdAt) {
  return {
    userId: uid,
    projectId: "proj_1",
    projectTitle: "Kyoto Cherry Blossoms",
    format: "PNG",
    widthPx: 3000,
    heightPx: 2000,
    dpi: 300,
    fileSizeBytes: 2450120,
    transparentBg: false,
    status: "COMPLETED",
    createdAt,
  };
}

function validPrefsPayload(uid, createdAt) {
  return {
    userId: uid,
    themePalette: "PASTEL_PINK",
    darkMode: "SYSTEM",
    languageCode: "en",
    notificationsEnabled: true,
    stripExifDefault: true,
    defaultDpi: 300,
    defaultUnit: "PX",
    cloudAutoSync: true,
    createdAt,
    updatedAt: createdAt,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any user collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 2000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("projects").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("projects")
      .doc("proj_1")
      .set(validProjectPayload(ALICE_UID, now))
  );
});

test("Authenticated owner can create, read, update, and delete own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Rahul Shah",
      email: "rahul@example.com",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Rahul S.",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject ghost field (shadow update test)
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject cross-user profile read (PII isolation)
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated owner: project create, list, resize update, and security guards", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 10000);

  // Create valid project (no @DocumentId `id` field)
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("projects")
      .doc("proj_1")
      .set(validProjectPayload(ALICE_UID, created))
  );

  // List own projects succeeds (matches Kotlin snapshots query)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("projects").get()
  );

  // Post-creation canvas resize partial update succeeds
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("projects")
      .doc("proj_1")
      .update({
        canvas: {
          width: 10.0,
          height: 8.0,
          unit: "IN",
          dpi: 300,
          aspectLocked: false,
          resizeMode: "EXTEND",
          orientation: "LANDSCAPE",
        },
        revision: 2,
        updatedAt: new Date(Date.now() - 1000),
      })
  );

  // Reject modifying immutable createdAt
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("projects")
      .doc("proj_1")
      .update({
        createdAt: new Date(Date.now() - 500),
        updatedAt: new Date(Date.now() - 500),
      })
  );

  // Reject extra @DocumentId 'id' field on create
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("projects")
      .doc("proj_2")
      .set({
        ...validProjectPayload(ALICE_UID, created),
        id: "proj_2",
      })
  );

  // Reject future tampered timestamp
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("projects")
      .doc("proj_3")
      .set(validProjectPayload(ALICE_UID, new Date(Date.now() + 60000)))
  );

  // Reject cross-user read, list, update, delete
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("projects")
      .doc("proj_1")
      .get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("projects").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("projects")
      .doc("proj_1")
      .delete()
  );
});

test("Exports subcollection: create/list/delete allowed for owner, update blocked (immutable history)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("exports")
      .doc("exp_1")
      .set(validExportPayload(ALICE_UID, created))
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("exports").get()
  );

  // Terminal export records cannot be modified
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("exports")
      .doc("exp_1")
      .update({ status: "FAILED" })
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("exports")
      .doc("exp_1")
      .delete()
  );
});

test("Preferences subcollection: create and update validated theme and settings", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("preferences")
      .doc("main")
      .set(validPrefsPayload(ALICE_UID, created))
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("preferences")
      .doc("main")
      .update({
        themePalette: "LAVENDER",
        darkMode: "DARK",
        updatedAt: new Date(Date.now() - 1000),
      })
  );

  // Reject invalid enum value
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("preferences")
      .doc("main")
      .update({
        themePalette: "NEON_INVALID",
        updatedAt: new Date(Date.now() - 500),
      })
  );
});
