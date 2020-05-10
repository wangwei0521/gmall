package com.xxxzz.gmall.list;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@SpringBootTest
public class GmallListServiceApplicationTests {

	@Autowired
	JestClient jestClient;
	@Test
	public void testEs() throws IOException {
		String query = "{\n" +
				"  \"query\": {\n" +
				"    \"match\": {\n" +
				"      \"name\": \"红海战役\"\n" +
				"    }\n" +
				"  }\n" +
				"}";
		Search search = new Search.Builder(query).addIndex("movie_chn").addType("movie").build();
		SearchResult searchResult = jestClient.execute(search);
		List<SearchResult.Hit<HashMap, Void>> hits = searchResult.getHits(HashMap.class);
		for (SearchResult.Hit<HashMap, Void> hit : hits){
			HashMap source = hit.source;
			System.out.println(source);
		}
	}

}
