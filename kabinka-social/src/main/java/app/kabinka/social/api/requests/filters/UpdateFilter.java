package app.kabinka.social.api.requests.filters;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Filter;
import app.kabinka.social.model.FilterAction;
import app.kabinka.social.model.FilterContext;
import app.kabinka.social.model.FilterKeyword;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UpdateFilter extends MastodonAPIRequest<Filter>{
	public UpdateFilter(String id, String title, EnumSet<FilterContext> context, FilterAction action, int expiresIn, List<FilterKeyword> words, List<String> deletedWords){
		super(HttpMethod.PUT, "/filters/"+id, Filter.class);

		List<KeywordAttribute> attrs=Stream.of(
				words.stream().map(w->new KeywordAttribute(w.id, null, w.keyword, w.wholeWord)),
				deletedWords.stream().map(wid->new KeywordAttribute(wid, true, null, null))
		).flatMap(Function.identity()).collect(Collectors.toList());
		setRequestBody(new FilterRequest(title, context, action, expiresIn==0 ? null : expiresIn, attrs));
	}

	@Override
	protected String getPathPrefix(){
		return "/api/v2";
	}
}
