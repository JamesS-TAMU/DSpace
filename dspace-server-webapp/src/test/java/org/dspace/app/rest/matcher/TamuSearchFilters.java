/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.matcher;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.hamcrest.Matcher;

public class TamuSearchFilters {

    private TamuSearchFilters() {};

    public static List<Matcher<? super Object>>  getSearchFilters() {
        return List.of(
            allOf(
                hasJsonPath("$.filter", is("department")),
                hasJsonPath("$.hasFacets", is(false)),
                hasJsonPath("$.type", is("text")),
                hasJsonPath("$.openByDefault", is(false)),
                SearchFilterMatcher.checkOperators()),
            allOf(
                    hasJsonPath("$.filter", is("Keyword")),
                    hasJsonPath("$.hasFacets", is(false)),
                    hasJsonPath("$.type", is("text")),
                    hasJsonPath("$.openByDefault", is(false)),
                    SearchFilterMatcher.checkOperators()),
            allOf(
                    hasJsonPath("$.filter", is("Abstract")),
                    hasJsonPath("$.hasFacets", is(false)),
                    hasJsonPath("$.type", is("text")),
                    hasJsonPath("$.openByDefault", is(false)),
                    SearchFilterMatcher.checkOperators()),
            allOf(
                    hasJsonPath("$.filter", is("Series")),
                    hasJsonPath("$.hasFacets", is(false)),
                    hasJsonPath("$.type", is("text")),
                    hasJsonPath("$.openByDefault", is(false)),
                    SearchFilterMatcher.checkOperators()),
            allOf(
                    hasJsonPath("$.filter", is("Sponsor")),
                    hasJsonPath("$.hasFacets", is(false)),
                    hasJsonPath("$.type", is("text")),
                    hasJsonPath("$.openByDefault", is(false)),
                    SearchFilterMatcher.checkOperators()),
            allOf(
                    hasJsonPath("$.filter", is("itemidentifier")),
                    hasJsonPath("$.hasFacets", is(false)),
                    hasJsonPath("$.type", is("text")),
                    hasJsonPath("$.openByDefault", is(false)),
                    SearchFilterMatcher.checkOperators()),
            allOf(
                    hasJsonPath("$.filter", is("LanguageISO")),
                    hasJsonPath("$.hasFacets", is(false)),
                    hasJsonPath("$.type", is("text")),
                    hasJsonPath("$.openByDefault", is(false)),
                    SearchFilterMatcher.checkOperators()),
            allOf(
                    hasJsonPath("$.filter", is("ETDLevel")),
                    hasJsonPath("$.hasFacets", is(false)),
                    hasJsonPath("$.type", is("text")),
                    hasJsonPath("$.openByDefault", is(false)),
                    SearchFilterMatcher.checkOperators()),
            allOf(
                    hasJsonPath("$.filter", is("ETDDiscipline")),
                    hasJsonPath("$.hasFacets", is(false)),
                    hasJsonPath("$.type", is("text")),
                    hasJsonPath("$.openByDefault", is(false)),
                    SearchFilterMatcher.checkOperators()),
            allOf(
                    hasJsonPath("$.filter", is("ETDGrantor")),
                    hasJsonPath("$.hasFacets", is(false)),
                    hasJsonPath("$.type", is("text")),
                    hasJsonPath("$.openByDefault", is(false)),
                    SearchFilterMatcher.checkOperators()),
            allOf(
                    hasJsonPath("$.filter", is("ETDAuthors")),
                    hasJsonPath("$.hasFacets", is(false)),
                    hasJsonPath("$.type", is("text")),
                    hasJsonPath("$.openByDefault", is(false)),
                    SearchFilterMatcher.checkOperators()),
            allOf(
                    hasJsonPath("$.filter", is("ETDSubmissionDate")),
                    hasJsonPath("$.hasFacets", is(false)),
                    hasJsonPath("$.type", is("text")),
                    hasJsonPath("$.openByDefault", is(false)),
                    SearchFilterMatcher.checkOperators()),
            allOf(
                    hasJsonPath("$.filter", is("ETDChair")),
                    hasJsonPath("$.hasFacets", is(false)),
                    hasJsonPath("$.type", is("text")),
                    hasJsonPath("$.openByDefault", is(false)),
                    SearchFilterMatcher.checkOperators()),
            allOf(
                    hasJsonPath("$.filter", is("ETDCommitteeMember")),
                    hasJsonPath("$.hasFacets", is(false)),
                    hasJsonPath("$.type", is("text")),
                    hasJsonPath("$.openByDefault", is(false)),
                    SearchFilterMatcher.checkOperators()),
            allOf(
                    hasJsonPath("$.filter", is("Description")),
                    hasJsonPath("$.hasFacets", is(false)),
                    hasJsonPath("$.type", is("text")),
                    hasJsonPath("$.openByDefault", is(false)),
                    SearchFilterMatcher.checkOperators()),
            allOf(
                    hasJsonPath("$.filter", is("Coverage")),
                    hasJsonPath("$.hasFacets", is(false)),
                    hasJsonPath("$.type", is("text")),
                    hasJsonPath("$.openByDefault", is(false)),
                    SearchFilterMatcher.checkOperators()),
            allOf(
                    hasJsonPath("$.filter", is("Rights")),
                    hasJsonPath("$.hasFacets", is(false)),
                    hasJsonPath("$.type", is("text")),
                    hasJsonPath("$.openByDefault", is(false)),
                    SearchFilterMatcher.checkOperators()),
            allOf(
                    hasJsonPath("$.filter", is("IsPartOf")),
                    hasJsonPath("$.hasFacets", is(false)),
                    hasJsonPath("$.type", is("text")),
                    hasJsonPath("$.openByDefault", is(false)),
                    SearchFilterMatcher.checkOperators()),
            allOf(
                    hasJsonPath("$.filter", is("itemtype")),
                    hasJsonPath("$.hasFacets", is(false)),
                    hasJsonPath("$.type", is("text")),
                    hasJsonPath("$.openByDefault", is(false)),
                    SearchFilterMatcher.checkOperators())
        );
    }
}
