// Import all of Bootstrap's JS
import * as bootstrap from 'bootstrap'

import '../scss/app.scss';

import AOS from 'aos';
import 'aos/dist/aos.css'; // You can also use <link> for styles
AOS.init({
    duration: 1000,
    delay: 100,
});

import Typed from 'typed.js';
const options = {
    stringsElement: '#typed-strings',
    startDelay: 300,
    typeSpeed: 150,
    smartBackspace: false,
};
if (document.getElementById("typed-strings")) {
    new Typed("#typed", options);
}

import 'animate.css';

import $ from 'jquery'
window.jQuery = window.$ = $

// extend jQuery to add a function that does it animate css
$.fn.extend({
    animateCss: function(animationName, callback) {
        let animationEnd = (function(el) {
            let animations = {
                animation: 'animationend',
                OAnimation: 'oAnimationEnd',
                MozAnimation: 'mozAnimationEnd',
                WebkitAnimation: 'webkitAnimationEnd',
            };

            for (let t in animations) {
                if (el.style[t] !== undefined) {
                    return animations[t];
                }
            }
        })(document.createElement('div'));

        this.addClass('animate__animated ' + 'animate__' + animationName).one(animationEnd, function() {
            $(this).removeClass('animate__animated ' + 'animate__' + animationName);

            if (typeof callback === 'function') callback();
        });

        return this;
    },
});