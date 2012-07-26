(function($) {

    $.fn.seqSlideshow = function(options) {

        // Create tabs with jQuery UI
        var $tabs = this.tabs();

        // Size the width based on imgBox content
        var currWidth = parseInt($tabs.css('min-width'));
        $tabs.find(".ui-tabs-panel").each(function() {
            $(this).find(".imgBox, .objectBox").each(function() {
                tempWidth = $(this).outerWidth();
                if (tempWidth > currWidth) {
                    currWidth = tempWidth;
                }
            });
        });
        $tabs.width(currWidth + "px");

        // Add carent placeholders
        this.caretHolder = $('<span class="caret"></span>');
        $tabs.find(".ui-tabs-nav li").append(this.caretHolder);

        // Add previous/next buttons
        this.navbtns = $('<li class="slideshowNav" style="float: right;"><a href="#" class="slideshowPrev">' + lang['DSLIDE_PREV'] + '</a> <a href="#" class="slideshowNext">' + lang['DSLIDE_NEXT'] + '</a></li>');
        $tabs.find(".ui-tabs-nav").append(this.navbtns);
        $.fn.seqSlideshow.updateNav($tabs, 0);

        // Add button style
        $tabs.find(".ui-tabs-nav a").each(function()  {
            $(this).addClass("button");
        });


        // Fires when tab is selected (clicked) - does not fire on current tab
        $tabs.bind( "tabsshow", function(e, ui) {
            $.fn.seqSlideshow.updateNav($tabs, ui.index);
        });
    };

    // Update nav buttons (prev/next) when tab is changed
    $.fn.seqSlideshow.updateNav = function(n, i) {
        var prevBtn = n.find('.slideshowPrev');
        var nextBtn = n.find('.slideshowNext');

        if ((i-1) < 0) {
            // Disable button
            prevBtn.attr("disabled", "disabled").addClass("off");
            prevBtn.click(function(e) {
                e.preventDefault();
                return false;
            });
        } else {
            // Enable button
            prevBtn.attr("disabled", "").removeClass("off");
            prevBtn.click(function(e) {
                n.tabs('select', i - 1);
                e.preventDefault();
                return false;
            });
        }
        if ((i+1) >= n.tabs('length')) {
            // Disable button
            nextBtn.attr("disabled", "disabled").addClass("off");
            nextBtn.click(function(e) {
                e.preventDefault();
                return false;
            });
        } else {
            // Enable button
            nextBtn.attr("disabled", "").removeClass("off");
            nextBtn.click(function(e) {
                n.tabs('select', i + 1);
                e.preventDefault();
                return false;
            });
        }

    };

})(jQuery);