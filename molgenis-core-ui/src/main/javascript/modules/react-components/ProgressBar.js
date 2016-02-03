/**
 * This react component renders a progress bar.
 * 
 * @module ProgressBar
 * 
 * @param progressPct
 *            The percentage that represents the shown width of the bar
 * @param progressMessage
 *            Message shown within the bar
 * @param stats
 *            The status of the job, will affect bar color
 * @param active
 *            Wether the bar should be animated or not
 * 
 * @exports ProgressBarClass, ProgressBarFactory
 */

import React from 'react';
import DeepPureRenderMixin from './mixin/DeepPureRenderMixin'; 

var div = React.DOM.div;

var ProgressBarClass = React.createClass({
	mixins: [DeepPureRenderMixin],
	displayName: 'ProgressBar',
	propTypes: {
		progressPct: React.PropTypes.number.isRequired,
		progressMessage: React.PropTypes.string.isRequired,
		status: React.PropTypes.oneOf(['info', 'danger', 'success', 'warning', 'primary']).isRequired,
		active: React.PropTypes.bool
	},
	render: function() {
		var extraClassAttr;
		if(this.props.active) {
			extraClassAttr = this.props.active ? ' progress-bar-striped active' : '';
		}
		
		return <div className="progress">
			<div className={'progress-bar progress-bar-' + this.props.status + '' + extraClassAttr} 
				role="progressbar" style={{minWidth: '2em', 'width': this.props.progressPct+ '%'}}>
		    		{this.props.progressMessage}
		    </div>
		</div>
	}
});

export { ProgressBarClass };

export default React.createFactory(ProgressBarClass);