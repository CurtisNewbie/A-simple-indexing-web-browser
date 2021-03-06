package com.curtisnewbie.webBrowserModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class is used to handle the prefix OrQuery, e.g., or(banana,apple). It
 * is part of the recursion that the matches() method will call the
 * QueryBuilder.parse() to parse the sub-query of this OrQuery object, and then
 * calls the matches() of the sub-query. The sub-query can be an OrQuqery object
 * as well.
 * 
 * @author Yongjie Zhuang
 */
public class OrQuery implements Query {

	/**
	 * A Set of subqueries of this AndQuery, it can be more than two in case of the
	 * prefix form.
	 */
	private TreeSet<String> subQueryCollection;

	/**
	 * Instantiate OrQuery
	 * 
	 * @param subQuery a TreeSet<String> of sub-query.
	 */
	public OrQuery(TreeSet<String> subQuery) {
		this.subQueryCollection = subQuery;
	}

	/**
	 * It is part of the recursion that the matches() method will call the
	 * QueryBuilder.parse() to parse the sub-query of this AndQuery object, and then
	 * calls the matches() of the sub-query.
	 * 
	 * This method searches through the given WebIndex based on the query to find
	 * all the matched results. The OrQuery object finds the matched results of all
	 * the sub-query and puts them together.
	 * 
	 * @return a Set<WebDoc> that is found based on the query and the given
	 *         WebIndex.
	 * @param wind the WebIndex that is used to search through based on the query.
	 * 
	 */
	@Override
	public Set<WebDoc> matches(WebIndex wind) {
		List<Set<WebDoc>> subqueriesResults = new ArrayList<>();
		// Get the results of all the sub-queries
		for (String eachQuery : subQueryCollection) {
			Query subQuery = QueryBuilder.parse(eachQuery);
			subqueriesResults.add(subQuery.matches(wind));
		}
		Set<WebDoc> finalSubQueryResult = new TreeSet<>();
		for (Set<WebDoc> eachSet : subqueriesResults) {
			if (eachSet != null) {
				finalSubQueryResult.addAll(eachSet);
			}
		}
		return finalSubQueryResult;
	}

	/**
	 * <p>
	 * This method returns a String that indicates the type of the query and its
	 * subqueries.The subqueries are indicated using '[' and ']'.
	 * </p>
	 * <p>
	 * E.g., or(A,and(C,D)) -> OR([A],[and(C,D)])
	 * </p>
	 * 
	 * @return a string that indicates the type of this query as well as its
	 *         subqueries.
	 */
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder("OR(");
		Iterator<String> eachQuery = subQueryCollection.iterator();

		while (eachQuery.hasNext()) {
			stringBuilder.append("[" + eachQuery.next() + "]");
			if (eachQuery.hasNext()) {
				stringBuilder.append(",");
			}
		}
		stringBuilder.append(")");
		return stringBuilder.toString();
	}

}
