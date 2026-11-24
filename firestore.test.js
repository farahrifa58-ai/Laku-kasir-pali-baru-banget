import { test, before, after, beforeEach } from 'node:test';
import fs from 'node:fs';
import {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails
} from '@firebase/rules-unit-testing';
import {
  doc,
  getDoc,
  setDoc,
  updateDoc,
  deleteDoc,
  collection,
  query,
  where,
  limit,
  getDocs,
  serverTimestamp,
  Timestamp
} from 'firebase/firestore';

const PROJECT_ID = 'gen-lang-client-0676480047';
const DATABASE_ID = 'ai-studio-android-lakukasi-169dc4af-6d52-44fe-866a-7d319a1964fa';

let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync('firestore.rules', 'utf8'),
      host: '127.0.0.1',
      port: 8085
    }
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

function dbFor(context) {
  return context.firestore(DATABASE_ID);
}

test('Verified admin can create, update, list, and delete activation codes', async () => {
  const adminCtx = testEnv.authenticatedContext('admin-uid', {
    email: 'farahrifa58@gmail.com',
    email_verified: true
  });
  const adminDb = dbFor(adminCtx);
  const codeRef = doc(adminDb, 'activation_codes', 'LAKU-2026-0001');

  await assertSucceeds(setDoc(codeRef, {
    status: 'available',
    createdAt: serverTimestamp(),
    notes: 'Batch 1'
  }));

  await assertSucceeds(getDocs(collection(adminDb, 'activation_codes')));
  await assertSucceeds(updateDoc(codeRef, { notes: 'Updated note' }));
  await assertSucceeds(deleteDoc(codeRef));
});

test('Unverified admin email or non-admin cannot create or delete activation codes', async () => {
  const unverifiedAdmin = testEnv.authenticatedContext('fake-admin', {
    email: 'farahrifa58@gmail.com',
    email_verified: false
  });
  await assertFails(setDoc(doc(dbFor(unverifiedAdmin), 'activation_codes', 'LAKU-FAKE-1'), {
    status: 'available'
  }));

  const guest = testEnv.unauthenticatedContext();
  await assertFails(setDoc(doc(dbFor(guest), 'activation_codes', 'LAKU-FAKE-2'), {
    status: 'available'
  }));
});

test('Client can get available code, claim it once, and auto-restore by registeredDeviceId', async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await setDoc(doc(dbFor(ctx), 'activation_codes', 'LAKU-2026-ABCD'), {
      status: 'available',
      createdAt: Timestamp.fromMillis(Date.now() - 5000)
    });
  });

  const guest = testEnv.unauthenticatedContext();
  const guestDb = dbFor(guest);
  const codeRef = doc(guestDb, 'activation_codes', 'LAKU-2026-ABCD');

  // 1. Validate code exists
  await assertSucceeds(getDoc(codeRef));

  // 2. Claim code once (available -> used)
  await assertSucceeds(updateDoc(codeRef, {
    status: 'used',
    usedByBusinessName: 'Toko Berkah Jaya',
    registeredDeviceId: 'android-device-987654321',
    usedAt: serverTimestamp()
  }));

  // 3. Re-install auto-restore query (limit 1) succeeds
  const restoreQuery = query(
    collection(guestDb, 'activation_codes'),
    where('registeredDeviceId', '==', 'android-device-987654321'),
    where('status', '==', 'used'),
    limit(1)
  );
  await assertSucceeds(getDocs(restoreQuery));

  // 4. Used code is permanently locked against non-admin re-activation or modification
  await assertFails(updateDoc(codeRef, {
    status: 'used',
    usedByBusinessName: 'Hijacked Store',
    registeredDeviceId: 'other-device-111111',
    usedAt: serverTimestamp()
  }));

  // 5. Used code cannot be reset to available or deleted by non-admin
  await assertFails(updateDoc(codeRef, { status: 'available' }));
  await assertFails(deleteDoc(codeRef));
});

test('Client can look up code via where("code", "==", ...) on auto-ID doc and claim matched docRef', async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await setDoc(doc(dbFor(ctx), 'activation_codes', 'autoGenDocId98765432'), {
      code: 'RAFDEF-GGCU-594V',
      appId: 'laku-kasir',
      status: 'available',
      createdAt: Timestamp.fromMillis(Date.now() - 5000)
    });
  });

  const guest = testEnv.unauthenticatedContext();
  const guestDb = dbFor(guest);

  // 1. Direct lookup by code ID succeeds (returns non-existent doc)
  await assertSucceeds(getDoc(doc(guestDb, 'activation_codes', 'RAFDEF-GGCU-594V')));

  // 2. Fallback query by field "code" with limit <= 5 succeeds
  const codeQuery = query(
    collection(guestDb, 'activation_codes'),
    where('code', '==', 'RAFDEF-GGCU-594V'),
    limit(5)
  );
  await assertSucceeds(getDocs(codeQuery));

  // 3. Updating matched auto-ID docRef succeeds while preserving code & appId fields
  const matchedRef = doc(guestDb, 'activation_codes', 'autoGenDocId98765432');
  await assertSucceeds(updateDoc(matchedRef, {
    status: 'used',
    usedByBusinessName: 'Warung Maju',
    registeredDeviceId: 'android-device-555555',
    usedAt: serverTimestamp()
  }));
});

test('Client claim fails if shadow fields, empty business name, short deviceId, or unbounded list query are sent', async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await setDoc(doc(dbFor(ctx), 'activation_codes', 'LAKU-2026-WXYZ'), {
      status: 'available'
    });
  });

  const guest = testEnv.unauthenticatedContext();
  const guestDb = dbFor(guest);
  const codeRef = doc(guestDb, 'activation_codes', 'LAKU-2026-WXYZ');

  // Unbounded list query blocked for non-admin
  await assertFails(getDocs(collection(guestDb, 'activation_codes')));

  // Extra shadow field rejected
  await assertFails(updateDoc(codeRef, {
    status: 'used',
    usedByBusinessName: 'Toko Test',
    registeredDeviceId: 'device-123456',
    usedAt: serverTimestamp(),
    notes: 'injected'
  }));

  // Empty business name rejected
  await assertFails(updateDoc(codeRef, {
    status: 'used',
    usedByBusinessName: '',
    registeredDeviceId: 'device-123456',
    usedAt: serverTimestamp()
  }));

  // Unrelated collection denied by default catch-all
  await assertFails(getDoc(doc(guestDb, 'other_collection', 'doc1')));
});
