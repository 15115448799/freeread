package cui.shibing.freeread.app.novelhead;

import cui.shibing.freeread.model.NovelHead;
import cui.shibing.freeread.service.NovelHeadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("novelHead")
public class NovelHeadController {
    private static final String RECOMMEND_PAGE = "main/recommend";
    private static final String NOVEL_DETAILS_PAGE = "main/novel_detail";
    private static final String NOVEL_RANKLIST_PAGE = "left/novel_ranking";
    private static final String NOVEL_SEARCH_RESULT_PAGE = "main/recommend";
    @Autowired
    private NovelHeadService novelHeadService;

    @RequestMapping("recommend")
    public String recommend(Model model, @PageableDefault(value = 8) Pageable pageable) {
        if (pageable != null) {
            Page<NovelHead> recommendNovels = novelHeadService.searchByPopularity(pageable);
            model.addAttribute("pageRecommendNovels", recommendNovels);
        }
        return RECOMMEND_PAGE;
    }

    @RequestMapping("novelDetails")
    public String novelDetails(Model model, @RequestParam("novelId") String novelId) {
        if (!StringUtils.isEmpty(novelId)) {
            NovelHead novelHead = novelHeadService.searchByNovelId(novelId);
            model.addAttribute("novelHead", novelHead);
        }
        return NOVEL_DETAILS_PAGE;
    }

    @RequestMapping("novelRankingList")
    public String novelRankingList(Model model, @PageableDefault() Pageable pageable) {
        if (pageable != null) {
            Page<NovelHead> novels = novelHeadService.searchByPopularity(pageable);
            model.addAttribute("pagePopularityNovels", novels);
        }
        return NOVEL_RANKLIST_PAGE;
    }

    @RequestMapping("novelSearchResult")
    public String novelSearchResult(Model model, @RequestParam("searchNovelName") String searchNovelName,
                                    @PageableDefault(value = 6) Pageable pageable) {
        if (pageable != null && !StringUtils.isEmpty(searchNovelName)) {
            model.addAttribute("pageRecommendNovels", novelHeadService.searchByNovelName(searchNovelName, pageable));
        }
        return NOVEL_SEARCH_RESULT_PAGE;
    }

}