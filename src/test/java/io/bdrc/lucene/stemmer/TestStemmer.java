package io.bdrc.lucene.stemmer;

import org.junit.Test;

public class TestStemmer {

	@Test
	public void testTrie() {
		Trie t = new Trie(true);

		String keys[] = {"a", "ba", "bb", "c"};
		String vals[] = {"1", "2", "2", "4"};

		for (int i = 0; i < keys.length; i++) {
			t.add(keys[i], vals[i]);
		}

		assert(0 == t.root);
		assert(2 == t.rows.size());
		assert(3 == t.cmds.size());   
		assertTrieContents(t, keys, vals);
	}

	@Test
	public void testTrieBackwards() {
		Trie t = new Trie(false);

		String keys[] = {"a", "ba", "bb", "c"};
		String vals[] = {"1", "2", "2", "4"};

		for (int i = 0; i < keys.length; i++) {
			t.add(keys[i], vals[i]);
		}

		assertTrieContents(t, keys, vals);
	}

	@Test
	public void testMultiTrie() {
		Trie t = new MultiTrie(true);

		String keys[] = {"a", "ba", "bb", "c"};
		String vals[] = {"1", "2", "2", "4"};

		for (int i = 0; i < keys.length; i++) {
			t.add(keys[i], vals[i]);
		}

		assertTrieContents(t, keys, vals);   
	}

	@Test
	public void testMultiTrie2() {
		Trie t = new MultiTrie2(true);

		String keys[] = {"a", "ba", "bb", "c"};
		String vals[] = {"1111", "2222", "2223", "4444"};

		for (int i = 0; i < keys.length; i++) {
			t.add(keys[i], vals[i]);
		}

		assertTrieContents(t, keys, vals);   
	}

	@Test
	public void testMultiTrie2Backwards() {
		Trie t = new MultiTrie2(false);

		String keys[] = {"a", "ba", "bb", "c"};
		String vals[] = {"1111", "2222", "2223", "4444"};

		for (int i = 0; i < keys.length; i++) {
			t.add(keys[i], vals[i]);
		}

		assertTrieContents(t, keys, vals);   
	}

	private static void assertTrieContents(Trie trie, String keys[], String vals[]) {
		Trie[] tries = new Trie[] {
				trie,
				trie.reduce(new Optimizer()),
				trie.reduce(new Optimizer2()),
				trie.reduce(new Gener()),
				trie.reduce(new Lift(true)),
				trie.reduce(new Lift(false))
		};

		for (Trie t : tries) {
			for (int i = 0; i < keys.length; i++) {
				assert(vals[i].equals(t.getFully(keys[i]).toString()));
				assert(vals[i].equals(t.getLastOnPath(keys[i]).toString()));
			}
		}
	}
}
