package com.bluebox.search;

import java.io.IOException;
import java.io.StringWriter;
import java.util.UUID;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.codehaus.jettison.json.JSONArray;
import org.junit.Test;

import com.bluebox.TestUtils;
import com.bluebox.smtp.storage.BlueboxMessage;
import com.bluebox.smtp.storage.StorageFactory;

public class SearchIndexerTest extends TestCase {
	private static final Logger log = Logger.getAnonymousLogger();
	private String uid1 = UUID.randomUUID().toString();
	private String uid2 = UUID.randomUUID().toString();
	private String uid3 = UUID.randomUUID().toString();
	private String uid4 = UUID.randomUUID().toString();


	@Override
	protected void setUp() throws Exception {
		getSearchIndexer().deleteIndexes();
		getSearchIndexer().addDoc(uid1,"receiever1@here.com","[sender1@there.com]","Subject in action","Lucene in Action","<b>Lucene in Action</b>", "receiever1@here.com",23423,6346543,false);
		getSearchIndexer().addDoc(uid2,"receiever2@here.com","[sender2@there.com]","Subject for dummies","Lucene for Dummies","<b>Lucene for dummies</b>",  "receiever2@here.com",235324,6346543,false);
		getSearchIndexer().addDoc(uid3,"receiever3@here.com","[sender3@there.com]","Subject for gigabytes", "Managing Gigabytes","<b>stephen</b><i>johnson</i>",  "receiever3@here.com",7646,6346543,false);
		getSearchIndexer().addDoc(uid4,"receiever4@here.com","[sender4@there.com]","Subject for Computer Science","The Art of Computer Science","<b>Lucene for Computer Science</b>",  "receiever4@here.com",543,6346543,false);
		for (int i = 0; i < 50; i++) {
			getSearchIndexer().addDoc(UUID.randomUUID().toString(),"xxx@xxx.com","[xxx@xxx.com]","ttttttttttttttttttttttttttt","tttttttttttttttttttttttttt","tttttttttttttttttttttttttttt",  "xxx@xxx.com",543,6346543,false);			
		}
		getSearchIndexer().commit(true);
	}

	@Override
	protected void tearDown() throws Exception {
		getSearchIndexer().deleteIndexes();
		getSearchIndexer().stop();
	}

	public SearchIf getSearchIndexer() throws Exception {
		return SearchFactory.getInstance();
	}

	@Test
	public void testHtmlSearch() throws IOException, Exception {
		assertEquals("Missing expected search results",4,getSearchIndexer().search(SearchUtils.substringQuery("sender"),SearchUtils.SearchFields.ANY,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
	}

	@Test
	public void testMultiWord() throws IOException, Exception {
		assertEquals("Missing expected search results",1,getSearchIndexer().search("Art of Computer",SearchUtils.SearchFields.BODY,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
		assertEquals("Missing expected search results",1,getSearchIndexer().search("for dummies",SearchUtils.SearchFields.SUBJECT,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
		assertEquals("Missing expected search results",1,getSearchIndexer().search("in action",SearchUtils.SearchFields.SUBJECT,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
		assertEquals("Missing expected search results",1,getSearchIndexer().search("Subject in action",SearchUtils.SearchFields.SUBJECT,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
		assertEquals("Missing expected search results",0,getSearchIndexer().search("Subject in action",SearchUtils.SearchFields.BODY,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
	}

	@Test
	public void testSubjectSearch() throws IOException, Exception {
		assertEquals("Missing expected search results",1,getSearchIndexer().search("action",SearchUtils.SearchFields.SUBJECT,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
		assertEquals("Missing expected search results",1,getSearchIndexer().search("action",SearchUtils.SearchFields.ANY,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
	}

	@Test
	public void testFromSearch() throws IOException, Exception {
		assertEquals("Missing expected search results",1,getSearchIndexer().search("johnson",SearchUtils.SearchFields.ANY,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
		assertEquals("Missing expected search results",1,getSearchIndexer().search("stephen",SearchUtils.SearchFields.ANY,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
		assertEquals("Missing expected search results",1,getSearchIndexer().search("Lucene in Action",SearchUtils.SearchFields.ANY,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
	}

	@Test
	public void testRecipientSearch() throws IOException, Exception {
		assertEquals("Missing expected search results",1,getSearchIndexer().search("receiever1",SearchUtils.SearchFields.RECIPIENTS,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
		assertEquals("Missing expected search results",1,getSearchIndexer().search("receiever1@here.com",SearchUtils.SearchFields.RECIPIENTS,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
		assertEquals("Missing expected search results",1,getSearchIndexer().search("receiever2",SearchUtils.SearchFields.RECIPIENTS,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
		//		assertEquals("Missing expected search results",4,getSearchIndexer().search("receiever",SearchUtils.SearchFields.RECIPIENTS,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
	}

	@Test
	public void testMailIndexing() throws Exception {
		BlueboxMessage msg = TestUtils.addRandomDirect(StorageFactory.getInstance());
		getSearchIndexer().indexMail(msg,true);
//		assertEquals("Missing expected search results",1,getSearchIndexer().search(SearchUtils.substringQuery(msg.getSubject()),SearchUtils.SearchFields.ANY,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
		assertEquals("Missing expected search results",1,getSearchIndexer().search("steve",SearchUtils.SearchFields.INBOX,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
		assertEquals("Missing expected search results",1,getSearchIndexer().search(SearchUtils.plainQuery(msg.getInbox().toString()),SearchUtils.SearchFields.ANY,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
	}

	@Test
	public void testDelete() throws Exception {
		getSearchIndexer().deleteDoc(uid1);
		assertEquals("Missing expected search results",1,getSearchIndexer().search("johnson",SearchUtils.SearchFields.ANY,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
		assertEquals("Missing expected search results",1,getSearchIndexer().search("stephen",SearchUtils.SearchFields.ANY,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
		assertEquals("Missing expected search results",0,getSearchIndexer().search("Lucene in Action",SearchUtils.SearchFields.ANY,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
		assertEquals("Missing expected search results",0,getSearchIndexer().search("sender1@there.com",SearchUtils.SearchFields.FROM,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
		assertEquals("Missing expected search results",1,getSearchIndexer().search("sender2@there.com",SearchUtils.SearchFields.FROM,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
		getSearchIndexer().deleteDoc(uid2);
		assertEquals("Missing expected search results",0,getSearchIndexer().search("sender2@there.com",SearchUtils.SearchFields.FROM,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
	}

	@Test
	public void testTextSearch() throws IOException, Exception {
		Object[] hits = getSearchIndexer().search("lucene",SearchUtils.SearchFields.ANY,0,10,SearchUtils.SortFields.SORT_RECEIVED,false);
		assertEquals("Missing expected search results",3,hits.length);
		//		for(int i=0;i<hits.length;++i) {
		//			log.info((i + 1) + ". " + hits[i].get(SearchUtils.SearchFields.UID.name()));
		//		}
	}

	@Test
	public void testHtmlConvert() throws IOException {
		String htmlStr = "<html><title>title text</title><body>this is the body</body></html>";
		String textStr = SearchUtils.htmlToString(htmlStr);
		assertEquals("Html to text conversion failed","title text this is the body",textStr);
	}

	@Test
	public void testTypeAhead() throws Exception, IOException {
		Object[] results = getSearchIndexer().search(SearchUtils.autocompleteQuery("receiever"), SearchUtils.SearchFields.RECIPIENTS, 0, 199, SearchUtils.SortFields.SORT_RECEIVED,false);
		assertTrue("Missing autocomplete results",results.length==4);
		results = getSearchIndexer().search(SearchUtils.autocompleteQuery("receiever1"), SearchUtils.SearchFields.RECIPIENTS, 0, 199, SearchUtils.SortFields.SORT_RECEIVED,false);
		assertTrue("Missing autocomplete results",results.length>0);
	}

	@Test
	public void testSearch() throws Exception {
		StringWriter sw;
		JSONArray ja;

		// test search in from field
		sw = new StringWriter();
		String searchString = "sender";
		log.info("Looking for sender "+searchString);
		getSearchIndexer().searchInboxes(SearchUtils.substringQuery(searchString), sw, 0, 50, SearchUtils.SearchFields.FROM, SearchUtils.SortFields.SORT_RECEIVED, true);
		ja = new JSONArray(sw.toString());
		log.info(ja.toString(3));
		assertTrue("No 'Subject' found in search results",ja.length()>0);

		// test search in subject
		sw = new StringWriter();
		searchString = "Subject for gigabytes";
		getSearchIndexer().searchInboxes(searchString, sw, 0, 50, SearchUtils.SearchFields.SUBJECT, SearchUtils.SortFields.SORT_RECEIVED, true);
		ja = new JSONArray(sw.toString());
		assertTrue("Missing search results",ja.length()>0);
		assertEquals(ja.getJSONObject(0).get(BlueboxMessage.SUBJECT),"Subject for gigabytes");

		// search for last few chars of subject
		sw = new StringWriter();
		searchString = "gigabytes";
		getSearchIndexer().searchInboxes(searchString, sw, 0, 50, SearchUtils.SearchFields.SUBJECT, SearchUtils.SortFields.SORT_RECEIVED, true);
		ja = new JSONArray(sw.toString());
		log.info(searchString+"="+ja.toString(3));
		assertTrue("Missing search results",ja.length()>0);
		assertEquals("Subject for gigabytes",ja.getJSONObject(0).get(BlueboxMessage.SUBJECT));

		// search for first few chars of subject
		sw = new StringWriter();
		searchString = "Subject in";
		getSearchIndexer().searchInboxes(searchString, sw, 0, 50, SearchUtils.SearchFields.SUBJECT, SearchUtils.SortFields.SORT_RECEIVED, true);
		ja = new JSONArray(sw.toString());
		log.info(searchString+"="+ja.toString(3));
		assertTrue("Missing search results",ja.length()>0);
		for (int i = 0; i < ja.length(); i++) {
			assertTrue("Inaccurate search result found",ja.getJSONObject(i).get(BlueboxMessage.SUBJECT).toString().contains("Subject"));
		}

		// test search To:
		//		sw = new StringWriter();
		//		getSearchIndexer().searchInboxes(original.getProperty(BlueboxMessage.FROM), sw, 0, 50, SearchUtils.SearchFields.FROM,SearchUtils.SearchFields.FROM,true);
		//		ja = new JSONArray(sw.toString());
		//		log.info(ja.toString(3));
		//		assertTrue("No 'From' search results",ja.length()>0);
		//		assertEquals(ja.getJSONObject(0).get(BlueboxMessage.SUBJECT),original.getSubject());
		//		assertEquals(ja.getJSONObject(0).get(BlueboxMessage.FROM),original.getProperty(BlueboxMessage.FROM));
		//
		//		// test substring search
		//		sw = new StringWriter();
		//		getSearchIndexer().searchInboxes("steve", sw, 0, 50, SearchUtils.SearchFields.FROM, null, true);
		//		ja = new JSONArray(sw.toString());
		//		log.info(ja.toString(3));
		//		assertTrue("No substring search results",ja.length()>0);
		//		assertEquals(ja.getJSONObject(0).get(BlueboxMessage.SUBJECT),original.getSubject());
		//		assertEquals(ja.getJSONObject(0).get(BlueboxMessage.FROM),original.getProperty(BlueboxMessage.FROM));
	}

	@Test
	public void testContains() throws Exception {
		assertTrue("Did not find document by UID",getSearchIndexer().containsUid(uid1));
		assertTrue("Did not find document by UID",getSearchIndexer().containsUid(uid2));
		assertTrue("Did not find document by UID",getSearchIndexer().containsUid(uid3));
		assertTrue("Did not find document by UID",getSearchIndexer().containsUid(uid4));
		assertFalse("Unexpected UID found",getSearchIndexer().containsUid(UUID.randomUUID().toString()));
		getSearchIndexer().deleteDoc(uid1);
		assertFalse("Should not find deleted document by UID",getSearchIndexer().containsUid(uid1));
	}

	@Test
	public void testSearchPaging() throws Exception {
		assertEquals("Search did not limit results",10,getSearchIndexer().search(SearchUtils.substringQuery(""),SearchUtils.SearchFields.ANY,0,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
		assertEquals("Search did not limit results",10,getSearchIndexer().search(SearchUtils.substringQuery(""),SearchUtils.SearchFields.ANY,10,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
		assertEquals("Should not return results here",0,getSearchIndexer().search(SearchUtils.substringQuery(""),SearchUtils.SearchFields.ANY,1000,10,SearchUtils.SortFields.SORT_RECEIVED,false).length);
	}
}
